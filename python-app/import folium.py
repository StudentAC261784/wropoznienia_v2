import folium
import pandas as pd

# Read the CSV file
df = pd.read_csv('sample_route.csv', sep=';')

# Create a folium map centered around the first point
mymap = folium.Map(location=[df['lat'].iloc[0], df['lon'].iloc[0]], zoom_start=14)

# Add markers for each point
for index, row in df.iterrows():
    folium.Marker([row['lat'], row['lon']], popup=f"Point {index}").add_to(mymap)

# Add lines connecting consecutive points
# for i in range(len(df) - 1):
#     points = [[df['lat'].iloc[i], df['lon'].iloc[i]],
#               [df['lat'].iloc[i + 1], df['lon'].iloc[i + 1]]]
#     folium.PolyLine(locations=points, color='blue').add_to(mymap)

# Save the map to an HTML file
mymap.save('route_map.html')