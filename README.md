# MusicPlay

MusicPlay es una aplicacion web de reproduccion musical con autenticacion, biblioteca, playlists, administracion de catalogo y recomendaciones personalizadas.

## Requisitos

- Java 21
- PostgreSQL
- Python 3.11 o superior
- Maven Wrapper incluido en `back-end/musicplay`

## Base de datos

El esquema principal esta en:

```powershell
psql -U postgres -f sql\musicplay_postgresql_completo.sql
```

Configura credenciales en `.env` o variables de entorno:

```properties
DB_URL=jdbc:postgresql://localhost:5432/musicdb
DB_USERNAME=postgres
DB_PASSWORD=tu_password
```

## Backend Spring Boot

```powershell
cd back-end\musicplay
.\mvnw.cmd spring-boot:run
```

Spring Boot expone la API principal en `http://localhost:8080`.

## Servicio Python de recomendaciones

Instala dependencias:

```powershell
pip install -r tools\requirements-algoritmo.txt
```

Entrena el modelo y exporta artefactos:

```powershell
python tools\entrenar_algoritmo_musicplay.py
```

Levanta FastAPI:

```powershell
uvicorn tools.controllers.recomendaciones_controller:app --reload --port 8001
```

Spring Boot hace proxy hacia FastAPI usando:

```properties
musicplay.algoritmo-url=http://localhost:8001
```

El frontend debe llamar a Spring en `/api/algoritmo/...`; no llama directamente a FastAPI.

## Frontend

Abre `front-end/page/index.html` o sirve la carpeta `front-end` con un servidor estatico local.

Si se abre desde `localhost`, el cliente usa `http://localhost:8080` como API. En despliegue remoto usa la URL configurada en `front-end/app/api.js`.

## Verificacion rapida

```powershell
cd back-end\musicplay
.\mvnw.cmd test
cd ..\..
python -B -m py_compile tools\entrenar_algoritmo_musicplay.py tools\controllers\recomendaciones_controller.py
node --check front-end\app\home.js
node --check front-end\app\views\homeView.js
```

## Notas de despliegue

- Ejecuta Spring Boot y FastAPI como procesos separados.
- No subas `.env` ni credenciales reales al repositorio.
- Si las recomendaciones salen vacias para usuarios nuevos, el controlador Python devuelve un fallback basado en popularidad y artista preferido.
