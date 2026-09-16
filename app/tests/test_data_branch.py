import pytest
import joblib
import pandas as pd
import os
import json
from predict_crowd_mapbox import predict_7day_geojson_combined

# Use the correct model filename
MODEL_PATH = "random_forest_model_final.joblib"

# Test 1: Check model loads correctly 
@pytest.mark.skipif(not os.path.exists(MODEL_PATH), reason="Model file not available")
def test_model_loading():
    bundle = joblib.load(MODEL_PATH)
    assert "model" in bundle and "encoders" in bundle

# Test 2: Check CSV structure of zone data 
def test_zone_csv_structure():
    df = pd.read_csv("manhattan_zones.csv")
    assert all(col in df.columns for col in ['LocationID', 'lat', 'lon'])
    assert df['lat'].between(-90, 90).all()
    assert df['lon'].between(-180, 180).all()
    assert df['LocationID'].is_unique
    assert df.notnull().all().all()

# Test 3: Generate GeoJSON and validate structure 
@pytest.mark.skipif(not os.path.exists(MODEL_PATH), reason="Model file not available")
def test_geojson_output():
    sample_date = "2024-07-22 12:00"
    predict_7day_geojson_combined(sample_date)

    filename = f"geojson_7day_prediction_2024-07-22.geojson"
    assert os.path.exists(filename)

    with open(filename, "r") as f:
        data = json.load(f)

    assert data["type"] == "FeatureCollection"
    assert isinstance(data["features"], list) and len(data["features"]) > 0

    sample_feature = data["features"][0]
    props = sample_feature["properties"]
    assert "zone_id" in props and "predicted_people" in props
    assert "datetime" in props and "intensity" in props
