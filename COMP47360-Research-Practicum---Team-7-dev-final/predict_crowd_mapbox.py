import pandas as pd
import numpy as np
import joblib
import json
from datetime import datetime, timedelta

# Load model and encoders
model_bundle = joblib.load("random_forest_model_final.joblib")
model = model_bundle["model"]
encoders = model_bundle["encoders"]

# Load zone coordinates
zones_df = pd.read_csv("manhattan_zones.csv")  # Columns: LocationID, lat, lon
zones_df['zone_id'] = zones_df['LocationID'].astype(str)  # Ensure zone_id is str

# Helper: convert month to season
def get_season(month):
    if month in [12, 1, 2]: return "Winter"
    elif month in [3, 4, 5]: return "Spring"
    elif month in [6, 7, 8]: return "Summer"
    else: return "Fall"

# Main prediction function
def predict_7day_geojson_combined(start_time_str):
    start_time = pd.to_datetime(start_time_str)
    all_hours = [start_time + timedelta(hours=i) for i in range(7 * 24)]
    base_zones = zones_df[['zone_id', 'lat', 'lon']]

    # Build full prediction frame
    rows = []
    for dt in all_hours:
        hour = dt.hour
        weekday = dt.weekday()
        season = get_season(dt.month)
        for _, row in base_zones.iterrows():
            rows.append({
                "zone_id": row.zone_id,
                "lat": row.lat,
                "lon": row.lon,
                "datetime": dt,
                "hour": hour,
                "weekday": weekday,
                "season": season,
                "is_holiday": 0  
            })
    df_pred = pd.DataFrame(rows)

    # Time features
    df_pred['time_float'] = df_pred['hour']
    df_pred['time_sin'] = np.sin(2 * np.pi * df_pred['time_float'] / 24.0)
    df_pred['time_cos'] = np.cos(2 * np.pi * df_pred['time_float'] / 24.0)
    df_pred['is_peak'] = df_pred['hour'].isin([7, 8, 17, 18]).astype(int)

    # Interaction features
    df_pred['zone_weekday'] = df_pred['zone_id'] + '_' + df_pred['weekday'].astype(str)
    df_pred['zone_hour'] = df_pred['zone_id'] + '_' + df_pred['hour'].astype(str)

    # Save original zone_id before encoding
    df_pred['zone_id_original'] = df_pred['zone_id']

    # Encode categorical features manually
    cat_cols = ['zone_id', 'weekday', 'season', 'zone_weekday', 'zone_hour']
    for col in cat_cols:
        df_pred[col] = df_pred[col].astype(str)
        classes = encoders[col].classes_
        mapping = dict(zip(classes, encoders[col].transform(classes)))
        df_pred[col] = df_pred[col].map(mapping)

    # Final feature list
    feature_cols = cat_cols + ['time_sin', 'time_cos', 'is_peak', 'is_holiday']
    log_preds = model.predict(df_pred[feature_cols])
    df_pred['predicted_people'] = np.expm1(log_preds)

    # Normalize by hour for intensity
    df_pred['intensity'] = df_pred.groupby('datetime')['predicted_people'].transform(
        lambda x: (x - x.min()) / (x.max() - x.min() + 1e-6)
    )

    # Add 'day' field: number of days since prediction start
    df_pred["day"] = (df_pred["datetime"] - df_pred["datetime"].min()).dt.days

    # Build single combined geoJSON
    geo_features = []
    for _, row in df_pred.iterrows():
        geo_features.append({
            "type": "Feature",
            "geometry": {
                "type": "Point",
                "coordinates": [row.lon, row.lat]
            },
            "properties": {
                "zone_id": row.zone_id_original,  # Uses original zone_id string from CSV
                "datetime": row.datetime.strftime("%Y-%m-%d %H:%M"),
                "day": row.day,
                "hour": row.hour,
                "predicted_people": round(row.predicted_people, 1),
                "intensity": round(row.intensity, 4)
            }
        })

    geojson = {
        "type": "FeatureCollection",
        "features": geo_features
    }

    # Save file
    filename = f"geojson_7day_prediction_{start_time.strftime('%Y-%m-%d')}.geojson"
    with open(filename, "w") as f:
        json.dump(geojson, f)
    print(f" 7-day combined geoJSON has been saved as: {filename}")


# Run prediction with a sample datetime
predict_7day_geojson_combined("2024-07-22 12:00")
