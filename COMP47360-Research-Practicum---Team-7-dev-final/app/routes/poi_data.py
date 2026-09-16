from fastapi import APIRouter, HTTPException
from fastapi.responses import StreamingResponse
from app.async_database_connection import get_db_connection  
import json
import time
import logging

router = APIRouter()
logger = logging.getLogger(__name__)

@router.get("/poi_data")
async def get_poi_data():
    query = """
    SELECT id, name, address, category, shop, amenity, leisure, tourism, latitude, longitude, ST_AsGeoJSON(geom) AS geometry
    FROM poi_data
    """

    try:
        # Establish async connection to psql database.
        conn = await get_db_connection()
        rows = await conn.fetch(query)


        # Async generator that yeilds segments of the response piece by piece.
        async def feature_generator():
            yield '{"type": "FeatureCollection", "features": ['

            # Loop through each POI record in the psql database. 
            first = True
            for row in rows:
                geom_json = row['geometry'] #Parse geometry into dictionary.
                geometry = json.loads(geom_json)

                # Convert row to a dictionary and pop the geometry as this was extracted earlier.
                properties = dict(row)
                properties.pop('geometry')

                # Put all the previous steps together to construct a GeoJson feature object.
                feature = {
                    "type": "Feature",
                    "geometry": geometry,
                    "properties": properties
                }

                if not first:
                    yield ','
                else:
                    first = False

                yield json.dumps(feature)

            yield ']}'

        return StreamingResponse(feature_generator(), media_type="application/json")

    except Exception as e:
        logger.error(f"Error fetching POI data: {e}")
        raise HTTPException(status_code=500, detail="Internal server error")
