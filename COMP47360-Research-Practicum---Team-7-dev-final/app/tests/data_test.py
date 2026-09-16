# this file tests the manhattan_zones.csv file for data integrity and correctness
import pandas as pd
import pytest

DATA_PATH = "manhattan_zones.csv"

@pytest.fixture(scope="module")
def df():
    return pd.read_csv(DATA_PATH)

def test_columns_exist(df): 	# checking if all required columns are present    
    expected_columns = ["LocationID", "zone", "borough", "lat", "lon"]
    assert all(col in df.columns for col in expected_columns), "Missing expected columns"

def test_no_null_values(df):   # checking if there are any null values in the dataframe
    assert not df.isnull().values.any(), "CSV contains null values"

def test_lat_lon_ranges(df):  
    assert df['lat'].between(-90, 90).all(), "Latitude values are out of range"
    assert df['lon'].between(-180, 180).all(), "Longitude values are out of range"

def test_locationid_unique(df): # checking if LocationID values are unique
    assert df['LocationID'].is_unique, "LocationID values are not unique"

def test_borough_manhattan_only(df): # checking if all borough values are "Manhattan"
    assert df['borough'].str.strip().eq("Manhattan").all(), "Found non-Manhattan entries"

def test_lat_lon_precision(df): # checking if lat and lon are floats
    assert df['lat'].apply(lambda x: isinstance(x, float)).all(), "Latitudes are not floats"
    assert df['lon'].apply(lambda x: isinstance(x, float)).all(), "Longitudes are not floats"
