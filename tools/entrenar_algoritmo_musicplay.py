from __future__ import annotations

import argparse
import json
import os
import pickle
from pathlib import Path

import joblib
import pandas as pd
from sklearn.compose import TransformedTargetRegressor
from sklearn.impute import SimpleImputer
from sklearn.metrics import mean_absolute_error, mean_squared_error, precision_score, r2_score
from sklearn.model_selection import train_test_split
from sklearn.neighbors import KNeighborsRegressor
from sklearn.pipeline import Pipeline
from sklearn.preprocessing import StandardScaler
from sqlalchemy import create_engine


ROOT_DIR = Path(__file__).resolve().parents[1]
DEFAULT_OUTPUT_DIR = ROOT_DIR / "tools" / "artifacts" / "algoritmo"


def load_env_file(path: Path) -> None:
    if not path.exists():
        return

    for line in path.read_text(encoding="utf-8").splitlines():
        clean = line.strip()
        if not clean or clean.startswith("#") or "=" not in clean:
            continue

        key, value = clean.split("=", 1)
        os.environ.setdefault(key.strip(), value.strip().strip('"').strip("'"))


def get_database_url() -> str:
    load_env_file(ROOT_DIR / ".env")
    url = os.getenv("DB_URL", "jdbc:postgresql://localhost:5432/musicdb")

    if url.startswith("jdbc:postgresql://"):
        url = url.replace("jdbc:postgresql://", "postgresql+psycopg2://", 1)
        username = os.getenv("DB_USERNAME", "postgres")
        password = os.getenv("DB_PASSWORD", "1080")
        return url.replace("postgresql+psycopg2://", f"postgresql+psycopg2://{username}:{password}@", 1)

    return url


def read_tables(database_url: str) -> dict[str, pd.DataFrame]:
    engine = create_engine(database_url)

    queries = {
        "historial": """
            select
                h.usuario_id,
                h.cancion_id,
                h.fecha_reproduccion,
                coalesce(h.duracion_reproduccion, 0) as duracion_reproduccion
            from historial_reproduccion h
            where h.usuario_id is not null
              and h.cancion_id is not null
        """,
        "canciones": """
            select
                s.song_id as cancion_id,
                s.song_nombre,
                s.song_artista as artista_id,
                s.song_genero as genero_id,
                coalesce(a.artista_nombre, 'Sin artista') as artista_nombre,
                coalesce(g.nombre_genero, 'Sin genero') as genero_nombre
            from song s
            left join artista a on a.artista_id = s.song_artista
            left join genero g on g.id = s.song_genero
        """,
        "preferencias_artista": """
            select usuario_id, artista_id
            from usuario_artista_preferido
        """,
    }

    return {name: pd.read_sql_query(query, engine) for name, query in queries.items()}


def build_training_dataset(tables: dict[str, pd.DataFrame]) -> tuple[pd.DataFrame, pd.Series, pd.DataFrame, list[str]]:
    historial = tables["historial"].copy()
    canciones = tables["canciones"].copy()
    preferencias = tables["preferencias_artista"].copy()

    if historial.empty:
        raise ValueError("No hay historial de reproduccion para entrenar el modelo.")

    historial["fecha_reproduccion"] = pd.to_datetime(historial["fecha_reproduccion"], errors="coerce")
    last_play = historial["fecha_reproduccion"].max()

    interacciones = (
        historial.groupby(["usuario_id", "cancion_id"], as_index=False)
        .agg(
            reproducciones=("cancion_id", "size"),
            duracion_total=("duracion_reproduccion", "sum"),
            ultima_reproduccion=("fecha_reproduccion", "max"),
        )
    )
    interacciones["dias_desde_ultima"] = (
        last_play - interacciones["ultima_reproduccion"]
    ).dt.days.fillna(999).clip(lower=0)

    interacciones["score"] = (
        interacciones["reproducciones"] * 2.0
        + (interacciones["duracion_total"] / 60.0).clip(upper=30)
        + (1 / (1 + interacciones["dias_desde_ultima"])) * 3.0
    )

    usuarios = interacciones[["usuario_id"]].drop_duplicates()
    candidatos = usuarios.merge(canciones[["cancion_id"]], how="cross")
    dataset = candidatos.merge(
        interacciones[
            [
                "usuario_id",
                "cancion_id",
                "reproducciones",
                "duracion_total",
                "dias_desde_ultima",
                "score",
            ]
        ],
        on=["usuario_id", "cancion_id"],
        how="left",
    )

    dataset[["reproducciones", "duracion_total", "score"]] = dataset[
        ["reproducciones", "duracion_total", "score"]
    ].fillna(0)
    dataset["dias_desde_ultima"] = dataset["dias_desde_ultima"].fillna(999)

    dataset = dataset.merge(canciones, on="cancion_id", how="left")
    dataset = dataset.merge(
        preferencias.rename(columns={"artista_id": "artista_preferido_id"}),
        on="usuario_id",
        how="left",
    )
    dataset["coincide_artista_preferido"] = (
        dataset["artista_id"] == dataset["artista_preferido_id"]
    ).astype(int)

    popularidad_cancion = (
        interacciones.groupby("cancion_id", as_index=False)
        .agg(
            reproducciones_globales=("reproducciones", "sum"),
            usuarios_distintos=("usuario_id", "nunique"),
        )
    )
    dataset = dataset.merge(popularidad_cancion, on="cancion_id", how="left")
    dataset[["reproducciones_globales", "usuarios_distintos"]] = dataset[
        ["reproducciones_globales", "usuarios_distintos"]
    ].fillna(0)

    dataset["genero_nombre"] = dataset["genero_nombre"].fillna("Sin genero")
    dataset["artista_nombre"] = dataset["artista_nombre"].fillna("Sin artista")

    features = pd.get_dummies(
        dataset[
            [
                "reproducciones",
                "duracion_total",
                "dias_desde_ultima",
                "reproducciones_globales",
                "usuarios_distintos",
                "coincide_artista_preferido",
                "genero_nombre",
                "artista_nombre",
            ]
        ],
        columns=["genero_nombre", "artista_nombre"],
        prefix=["genero", "artista"],
        dtype=int,
    )

    target = dataset["score"]
    metadata = dataset[
        [
            "usuario_id",
            "cancion_id",
            "song_nombre",
            "artista_nombre",
            "genero_nombre",
            "reproducciones",
            "reproducciones_globales",
            "usuarios_distintos",
            "coincide_artista_preferido",
            "score",
        ]
    ].copy()
    return features, target, metadata, features.columns.tolist()


def train_model(features: pd.DataFrame, target: pd.Series) -> TransformedTargetRegressor:
    neighbors = min(7, max(1, len(features)))
    pipeline = Pipeline(
        steps=[
            ("imputer", SimpleImputer(strategy="median")),
            ("scaler", StandardScaler()),
            ("knn", KNeighborsRegressor(n_neighbors=neighbors, weights="distance")),
        ]
    )

    model = TransformedTargetRegressor(regressor=pipeline)
    model.fit(features, target)
    return model


def evaluate_model(features: pd.DataFrame, target: pd.Series) -> dict[str, float | int | None]:
    if len(features) < 5 or target.nunique() < 2:
        return {
            "precision_binaria": None,
            "mae": None,
            "rmse": None,
            "r2": None,
            "filas_prueba": 0,
        }

    test_size = 0.2 if len(features) >= 10 else 0.4
    x_train, x_test, y_train, y_test = train_test_split(
        features,
        target,
        test_size=test_size,
        random_state=42,
    )
    model = train_model(x_train, y_train)
    predictions = model.predict(x_test)
    threshold = max(0.01, float(y_train.quantile(0.75)))
    y_real_relevante = (y_test > 0).astype(int)
    y_pred_relevante = (predictions >= threshold).astype(int)
    precision = None

    if y_pred_relevante.sum() > 0:
        precision = float(precision_score(y_real_relevante, y_pred_relevante, zero_division=0))

    return {
        "precision_binaria": precision,
        "mae": float(mean_absolute_error(y_test, predictions)),
        "rmse": float(mean_squared_error(y_test, predictions) ** 0.5),
        "r2": float(r2_score(y_test, predictions)) if len(y_test) > 1 else None,
        "filas_prueba": int(len(x_test)),
    }


def export_artifacts(
        model: TransformedTargetRegressor,
        features: pd.DataFrame,
        target: pd.Series,
        metadata: pd.DataFrame,
        columns: list[str],
        output_dir: Path,
        suffix: str = "") -> dict[str, float | int | None]:
    output_dir.mkdir(parents=True, exist_ok=True)
    suffix_part = f"_{suffix}" if suffix else ""

    with (output_dir / f"modelo{suffix_part}.pkl").open("wb") as model_file:
        pickle.dump(model, model_file)

    joblib.dump(model, output_dir / f"modelo_knn_musicplay{suffix_part}.joblib")
    feature_export = features.drop(columns=metadata.columns.intersection(features.columns), errors="ignore")
    pd.concat([metadata, feature_export], axis=1).to_csv(
        output_dir / f"dataset_entrenamiento{suffix_part}.csv",
        index=False,
    )

    recomendaciones = metadata.copy()
    recomendaciones["score_predicho"] = model.predict(features)
    popularidad_maxima = recomendaciones["reproducciones_globales"].max()
    usuarios_maximos = recomendaciones["usuarios_distintos"].max()
    recomendaciones["popularidad_normalizada"] = 0.0

    if popularidad_maxima > 0:
        recomendaciones["popularidad_normalizada"] += (
            recomendaciones["reproducciones_globales"] / popularidad_maxima
        )

    if usuarios_maximos > 0:
        recomendaciones["popularidad_normalizada"] += (
            recomendaciones["usuarios_distintos"] / usuarios_maximos
        )

    recomendaciones["score_recomendacion"] = (
        recomendaciones["score_predicho"]
        + recomendaciones["popularidad_normalizada"] * 0.75
        + recomendaciones["coincide_artista_preferido"] * 3.0
    )
    recomendaciones = (
        recomendaciones[recomendaciones["reproducciones"] == 0]
        .sort_values(["usuario_id", "score_recomendacion"], ascending=[True, False])
        .groupby("usuario_id")
        .head(10)
    )
    recomendaciones.to_csv(output_dir / f"recomendaciones_por_usuario{suffix_part}.csv", index=False)

    (output_dir / f"columnas_modelo{suffix_part}.json").write_text(
        json.dumps(columns, ensure_ascii=False, indent=2),
        encoding="utf-8",
    )

    evaluation = evaluate_model(features, target)
    metrics = {
        "filas_entrenamiento": int(len(features)),
        "variables_modelo": int(len(columns)),
        "score_promedio": float(target.mean()),
        "score_maximo": float(target.max()),
        **evaluation,
    }
    (output_dir / f"metricas_entrenamiento{suffix_part}.json").write_text(
        json.dumps(metrics, ensure_ascii=False, indent=2),
        encoding="utf-8",
    )
    return metrics


def train_and_export(
        database_url: str | None = None,
        output_dir: str | Path = DEFAULT_OUTPUT_DIR,
        usuario_id: int | None = None) -> dict[str, float | int | None]:
    tables = read_tables(database_url or get_database_url())
    features, target, metadata, columns = build_training_dataset(tables)

    suffix = ""
    if usuario_id is not None:
        mask = metadata["usuario_id"] == usuario_id
        features = features.loc[mask].reset_index(drop=True)
        target = target.loc[mask].reset_index(drop=True)
        metadata = metadata.loc[mask].reset_index(drop=True)
        suffix = f"usuario_{usuario_id}"

        if features.empty:
            raise ValueError(f"No hay datos suficientes para entrenar el usuario {usuario_id}.")

    model = train_model(features, target)
    return export_artifacts(model, features, target, metadata, columns, Path(output_dir), suffix)


def main() -> None:
    parser = argparse.ArgumentParser(description="Entrena el algoritmo de recomendacion de MusicPlay.")
    parser.add_argument("--database-url", default=get_database_url(), help="URL SQLAlchemy de PostgreSQL.")
    parser.add_argument("--output-dir", default=str(DEFAULT_OUTPUT_DIR), help="Carpeta para guardar artefactos.")
    args = parser.parse_args()

    metrics = train_and_export(args.database_url, args.output_dir)

    print("Entrenamiento completado.")
    print(json.dumps(metrics, ensure_ascii=False, indent=2))


if __name__ == "__main__":
    main()
