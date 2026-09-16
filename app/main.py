from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from fastapi.middleware.gzip import GZipMiddleware
from fastapi.staticfiles import StaticFiles
from app.async_database_connection import init_db_pool     
from app.routes import zone_predictions, health, poi_data
from contextlib import asynccontextmanager
from prometheus_fastapi_instrumentator import Instrumentator
import logging

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

@asynccontextmanager
async def lifespan(app: FastAPI):
    try:
        logger.info("Initializing database pool")
        await init_db_pool()
        logger.info("Database pool initialized successfully")
        yield
    except Exception as e:
        logger.error(f"Connection pool failed to initialize: {e}")
        yield

app = FastAPI(lifespan=lifespan)

# Implemented general instrumentator which enables prometheus monitoring.
instrumentator = Instrumentator().add().instrument(app).expose(app)

# CORS - Restrict API call access to certain domains.
origins = [
    "http://localhost:3000", "http://127.0.0.1:3000",
    "http://localhost:5173", "http://127.0.0.1:5173",
    "http://localhost:8080", "http://127.0.0.1:8080",
    "http://localhost:8000", "http://127.0.0.1:8000",
    "http://localhost", "http://127.0.0.1",
    "http://137.43.49.23",
]
app.add_middleware(
    CORSMiddleware,
    allow_origins=origins,
    allow_credentials=False,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Implemented GZip compression due to the sheer size of the data. Reduces speed slightly but increase scalability.
app.add_middleware(GZipMiddleware, minimum_size=500)

app.include_router(zone_predictions.router, prefix="/api")
app.include_router(health.router, prefix="/api")
app.include_router(poi_data.router, prefix="/api")

app.mount("/", StaticFiles(directory="app/static", html=True), name="static")
