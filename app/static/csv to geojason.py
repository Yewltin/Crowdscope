import pandas as pd
import geopandas as gpd
from shapely.geometry import Point

"""
This code is necessary to create GeoJason format files from csv files, as heatmaps on MapBox uses GeoJason not csv
"""
# Load CSV
df = pd.read_csv('manhattan_heatmap_points_test.csv')

# Create point geometry
geometry = [Point(xy) for xy in zip(df['longitude'], df['latitude'])]

# Create GeoDataFrame
gdf = gpd.GeoDataFrame(df, geometry=geometry)

# Set Coordinate Reference System (CRS) to WGS84 (EPSG:4326)
gdf.set_crs(epsg=4326, inplace=True)

# Save as GeoJSON
gdf.to_file('manhattan_heatmap_points_test.geojson', driver='GeoJSON')

print("GeoJSON file created: manhattan_heatmap_points_test.geojson")