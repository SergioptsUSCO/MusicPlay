from __future__ import annotations

import sys
import os
from datetime import datetime
from pathlib import Path

import pandas as pd
from fastapi import FastAPI, HTTPException, Query
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel, Field
from sqlalchemy import create_engine, text


TOOLS_DIR = Path(__file__).resolve().parents[1]
if str(TOOLS_DIR) not in sys.path:
    sys.path.append(str(TOOLS_DIR))

from entrenar_algoritmo_musicplay import DEFAULT_OUTPUT_DIR, get_database_url, train_and_export


app = FastAPI(title="MusicPlay Recomendaciones", version="1.0.0")
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=False,
    allow_methods=["*"],
    allow_headers=["*"],
)


class HistorialRequest(BaseModel):
    cancion_id: int = Field(..., gt=0)
    duracion_reproduccion: int = Field(0, ge=0)
    reentrenar: bool = True


def ensure_artifacts() -> None:
    if not (DEFAULT_OUTPUT_DIR / "modelo.pkl").exists():
        train_and_export()


def read_recommendations() -> pd.DataFrame:
    ensure_artifacts()
    path = DEFAULT_OUTPUT_DIR / "recomendaciones_por_usuario.csv"

    if not path.exists():
        raise HTTPException(status_code=404, detail="No hay recomendaciones exportadas.")

    return pd.read_csv(path)


def build_fallback_recommendations(usuario_id: int, limite: int) -> list[dict]:
    engine = create_engine(get_database_url())

    query = text("""
        select
            s.song_id as cancion_id,
            s.song_nombre,
            coalesce(a.artista_nombre, 'Sin artista') as artista_nombre,
            coalesce(g.nombre_genero, 'Sin genero') as genero_nombre,
            0.0 as score_predicho,
            (
                coalesce(count(h.id), 0) * 1.0
                + case
                    when exists (
                        select 1
                        from usuario_artista_preferido uap
                        where uap.usuario_id = :usuario_id
                          and uap.artista_id = s.song_artista
                    )
                    then 3.0
                    else 0.0
                  end
            ) as score_recomendacion
        from song s
        left join artista a on a.artista_id = s.song_artista
        left join genero g on g.id = s.song_genero
        left join historial_reproduccion h on h.cancion_id = s.song_id
        where not exists (
            select 1
            from historial_reproduccion hu
            where hu.usuario_id = :usuario_id
              and hu.cancion_id = s.song_id
        )
        group by s.song_id, s.song_nombre, a.artista_nombre, g.nombre_genero, s.song_artista
        order by score_recomendacion desc, s.song_nombre asc
        limit :limite
    """)

    with engine.begin() as connection:
        rows = connection.execute(query, {"usuario_id": usuario_id, "limite": limite}).mappings().all()

    recommendations = []
    for row in rows:
        item = dict(row)
        item["score_predicho"] = float(item["score_predicho"])
        item["score_recomendacion"] = float(item["score_recomendacion"])
        recommendations.append(item)

    return recommendations


@app.get("/api/algoritmo/usuarios/{usuario_id}/recomendaciones")
@app.get("/usuarios/{usuario_id}/recomendaciones")
def recomendaciones_usuario(
        usuario_id: int,
        limite: int = Query(10, ge=1, le=50)) -> dict:
    recomendaciones = read_recommendations()
    recomendaciones_usuario_df = recomendaciones[
        recomendaciones["usuario_id"] == usuario_id
    ].head(limite)

    recomendaciones = recomendaciones_usuario_df[
        [
            "cancion_id",
            "song_nombre",
            "artista_nombre",
            "genero_nombre",
            "score_predicho",
            "score_recomendacion",
        ]
    ].to_dict(orient="records")

    origen = "modelo"
    if not recomendaciones:
        recomendaciones = build_fallback_recommendations(usuario_id, limite)
        origen = "fallback"

    return {
        "usuario_id": usuario_id,
        "total": int(len(recomendaciones)),
        "origen": origen,
        "recomendaciones": recomendaciones,
    }


@app.post("/api/algoritmo/usuarios/{usuario_id}/historial")
@app.post("/usuarios/{usuario_id}/historial")
def monitorear_historial(usuario_id: int, request: HistorialRequest) -> dict:
    engine = create_engine(get_database_url())

    with engine.begin() as connection:
        existe_usuario = connection.execute(
            text("select 1 from usuario where usuario_id = :usuario_id"),
            {"usuario_id": usuario_id},
        ).first()
        existe_cancion = connection.execute(
            text("select 1 from song where song_id = :cancion_id"),
            {"cancion_id": request.cancion_id},
        ).first()

        if not existe_usuario:
            raise HTTPException(status_code=404, detail="Usuario no encontrado.")

        if not existe_cancion:
            raise HTTPException(status_code=404, detail="Cancion no encontrada.")

        connection.execute(
            text("""
                insert into historial_reproduccion (
                    usuario_id,
                    cancion_id,
                    fecha_reproduccion,
                    duracion_reproduccion
                )
                values (
                    :usuario_id,
                    :cancion_id,
                    :fecha_reproduccion,
                    :duracion_reproduccion
                )
            """),
            {
                "usuario_id": usuario_id,
                "cancion_id": request.cancion_id,
                "fecha_reproduccion": datetime.utcnow(),
                "duracion_reproduccion": request.duracion_reproduccion,
            },
        )

    response = {
        "message": "Historial registrado.",
        "usuario_id": usuario_id,
        "cancion_id": request.cancion_id,
        "reentrenado": False,
    }

    if request.reentrenar:
        metricas_globales = train_and_export()
        metricas_usuario = train_and_export(usuario_id=usuario_id)
        response.update(
            {
                "reentrenado": True,
                "modelo_global": str(DEFAULT_OUTPUT_DIR / "modelo.pkl"),
                "modelo_usuario": str(DEFAULT_OUTPUT_DIR / f"modelo_usuario_{usuario_id}.pkl"),
                "metricas_globales": metricas_globales,
                "metricas_usuario": metricas_usuario,
            }
        )

    return response


@app.post("/api/algoritmo/entrenar")
@app.post("/entrenar")
def entrenar_modelo() -> dict:
    metricas = train_and_export()
    return {
        "message": "Modelo entrenado.",
        "modelo": str(DEFAULT_OUTPUT_DIR / "modelo.pkl"),
        "metricas": metricas,
    }


if __name__ == "__main__":
    import uvicorn

    port = int(os.getenv("SERVER_PORT", "8001"))
    uvicorn.run("tools.controllers.recomendaciones_controller:app", host="0.0.0.0", port=port, reload=True)
