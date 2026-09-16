# This file tests all backend API endpoints using FastAPI's TestClient

from fastapi.testclient import TestClient
from app.main import app

client = TestClient(app)

def test_health_check():
    response = client.get("/api/health")
    assert response.status_code == 200
    assert response.json() == {"status": "ok"}

def test_zone_predictions_status_code():
    response = client.get("/api/zone-predictions?date=2023-07-01")
    # 200 = success with data, 404 = no data for date, both are acceptable
    assert response.status_code in [200, 404]

def test_zone_predictions_format():
    response = client.get("/api/zone-predictions?date=2023-07-01")
    
    if response.status_code == 200:
        data = response.json()
        assert "type" in data
        assert data["type"] == "FeatureCollection"
        assert "features" in data
        assert isinstance(data["features"], list)
    else:
        #fallback for when no data is made available
        assert response.status_code == 404
