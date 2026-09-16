# This code is part of a test suite for edge cases in a FastAPI application.
# It includes tests for seasonal transitions, extreme weather simulations, boundary values

import pytest
from fastapi.testclient import TestClient
from app.main import app

client = TestClient(app)

# SCENARIO 1: Seasonal Transition Edge
def test_seasonal_transition_predictions():
    """
    Scenario: Late spring (May), valid timeframe.
    Expectation: Endpoint returns FeatureCollection with features list.
    """
    response = client.get("/api/zone-predictions")
    assert response.status_code == 200
    data = response.json()
    assert data.get("type") == "FeatureCollection"
    assert isinstance(data.get("features"), list)

# SCENARIO 2: Extreme Weather Simulation
def test_extreme_weather_simulation():
    """
    Scenario: Simulated abnormal input (heatwave-like).
    Expectation: System handles gracefully, returns 422 or sanitized response.
    """
    payload = {
        "temperature": 50.0,
        "humidity": 99,
        "month": "July",
        "hour": 14,
        "day": "Tuesday"
    }
    response = client.post("/api/predict", json=payload)
    assert response.status_code in [200, 422, 400]

# SCENARIO 3: Boundary Values on Hour and Month
@pytest.mark.parametrize("hour", [0, 23, -1, 24])
def test_hour_boundary_values(hour):
    """
    Scenario: Test boundary and out-of-range hours.
    Expectation: Valid hours accepted; invalid hours return 422.
    """
    payload = {"hour": hour, "month": "June", "temperature": 20, "humidity": 50, "day": "Monday"}
    response = client.post("/api/predict", json=payload)
    if 0 <= hour <= 23:
        assert response.status_code == 200
    else:
        assert response.status_code == 422

# SCENARIO 4: Invalid Categorical Inputs 
def test_invalid_categorical_inputs():
    """
    Scenario: Undefined month or weekday in input.
    Expectation: Endpoint responds with 422 (validation error).
    """
    payload = {"hour": 12, "month": "Smarch", "temperature": 20, "humidity": 50, "day": "Funday"}
    response = client.post("/api/predict", json=payload)
    assert response.status_code == 422

# SCENARIO 5: Missing Required Fields 
def test_missing_required_fields():
    """
    Scenario: Missing 'temperature' and 'humidity'.
    Expectation: Returns 422 due to validation failure.
    """
    payload = {"hour": 12, "month": "June", "day": "Monday"}  # Missing temp/humidity
    response = client.post("/api/predict", json=payload)
    assert response.status_code == 422
