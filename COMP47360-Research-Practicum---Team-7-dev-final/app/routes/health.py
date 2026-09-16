from fastapi import APIRouter, HTTPException
from app.async_database_connection import get_db_connection
import logging

router = APIRouter()

logger = logging.getLogger(__name__)


#This route can be used to check if the fastapi app is up and running.
@router.get("/health")
async def health():
    logger.info("Health check received")
    return {"status": "ok"}

#This route can be used to check the status of the psql database and check if the Fastapi app is connected to the database.
@router.get("/health/db")
async def db_health():
    try:
        # Get a connection from the async connection pool
        conn = await get_db_connection()

        # Test the connection by running a simple query
        async with conn.acquire() as connection:
            result = await connection.fetchval("SELECT 1")

        if result == 1:
            return {"status": "ok", "database": "connected"}
        else:
            raise HTTPException(status_code=500, detail="Unexpected database result")

    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Database connection failed: {e}")