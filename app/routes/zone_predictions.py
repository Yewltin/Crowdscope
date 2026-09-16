from fastapi import HTTPException, APIRouter, Query
from fastapi.responses import StreamingResponse # Async streaming due to large datasets. 
from datetime import datetime, timedelta
from app.async_database_connection import get_db_connection  
import json
import logging

# Router incstance created to enable modular defining of endpoints.
router = APIRouter()

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

# This route takes a date as a parameter, establishes a connection psql database, queries the db for the relevant data and streams the data back to the frontend in chunks.
# Each row that is fetched from the database is encoded as a GeoJson feature and sent one at a time.
@router.get("/zone_predictions")
async def get_zone_predictions_stream(date: str = Query(..., description="Date in YYYY-MM-DD format")):
    logger.info(f"Received request for zone predictions for date: {date}")

    # Parse string to datetime object. 400 error if incorrect format.
    try:
        requested_date = datetime.strptime(date, "%Y-%m-%d")
    except ValueError:
            raise HTTPException(status_code=400, detail="Invalid date format. Use YYYY-MM-DD.")

    # start_datetime and end_datetime are used to query the psql database. 
    start_datetime = requested_date
    end_datetime = requested_date + timedelta(days=1)

    logger.info(f"Filtering data between {start_datetime} and {end_datetime}")

    # Query database and return only the features that are required for rendering the heatmap.
    query = """
    SELECT hour, intensity, ST_AsGeoJSON(geom) AS geometry
    FROM forcasted_data_annual
    WHERE datetime >= $1 AND datetime < $2
    """

    try: 
        conn = await get_db_connection() # async connection to psql database. 

        async with conn.acquire() as connection:
            async with connection.transaction():

                rows = await conn.fetch(query, start_datetime, end_datetime) # transaction initiated.

                # Async generator that yeilds segments of the response piece by piece (async)
                # Improves performance and memory efficiency.
                async def feature_generator():
                    try:
                        yield '{"type": "FeatureCollection", "features": ['
                        first = True

                        # If no row return a empty list and log for debugging. 
                        if not rows:
                            logger.warning("No data found for the requested date")
                            yield ']}'
                            return

                        # Iterates through each row and extracts data.
                        for row in rows:
                            hour = row['hour']
                            intensity = row['intensity']
                            geom_json = row['geometry']
                            geometry = json.loads(geom_json)

                            # Constructs GeoJson feature with the previousl extracted data.
                            feature = {
                                "type": "Feature",
                                "geometry": geometry,
                                "properties": {
                                    "hour": hour,
                                    "intensity": float(intensity),
                                }
                            }

                            # Comma is the delimiter 
                            if not first:
                                yield ','
                            else:
                                first = False
                            yield json.dumps(feature)

                        yield ']}'

                    except Exception as e:
                        logger.error(f"Error in feature_generator: {e}")
                        yield ']}'

                return StreamingResponse(feature_generator(), media_type="application/json")

    except Exception as e:
        logger.error(f"Error in /api/zone_predictions: {e}")
        raise HTTPException(status_code=500, detail=str(e))
