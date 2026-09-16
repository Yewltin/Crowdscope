# this file tests if our database connection is made successfuly

import os
import pytest
import psycopg2
from dotenv import load_dotenv
from pathlib import Path

# loads .env file from directory above
env_path = Path(__file__).resolve().parent.parent / ".env"
load_dotenv(dotenv_path=env_path, override=True)

def test_database_connection(): 
    try:
        conn = psycopg2.connect(
            host=os.getenv("DB_HOST"),
            port=os.getenv("DB_PORT"),
            user=os.getenv("DB_USER"),
            password=os.getenv("DB_PASSWORD"),
            dbname=os.getenv("DB_NAME")
        )
        assert conn is not None
        conn.close()
    except Exception as e:
        pytest.fail(f"Database connection failed: {e}")
