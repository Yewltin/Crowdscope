# This program handles the initialization of a pool of asynchronous database connections, and contains functionality for a database connection to be fetched.
# The fact that these database connections are reusable results in reduced database connection overhead. Is very fast and efficient.

import os
import asyncpg
from dotenv import load_dotenv

load_dotenv()

#Declaration of global variable.
db_pool = None

# Initialize the connection pool. The pool contains reusable database connections. 
async def init_db_pool():
    global db_pool

    db_pool = await asyncpg.create_pool(
        user=os.getenv("DB_USER"),
        password=os.getenv("DB_PASSWORD"),
        host=os.getenv("DB_HOST"),
        port=int(os.getenv("DB_PORT")),
        database=os.getenv("DB_NAME"),
        ssl=False,                       
        min_size=1,
        max_size=10,
        timeout=10                         
    )
    print("Database connection pool initialized.")

# Function to fetch a connection from the pool.
async def get_db_connection():
    global db_pool
    if not db_pool:
        await init_db_pool()
    return db_pool
