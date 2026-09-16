// Mapbox API Restricted Token
mapboxgl.accessToken = 'pk.eyJ1Ijoic3Rldm84NzMiLCJhIjoiY21jbDQxMnhqMDVlczJrc2RiYWtycXg2byJ9.OlC7Exf_yKvcgt_J5rAwYQ';


function getQueryParam(param) {
  const urlParams = new URLSearchParams(window.location.search);
  return urlParams.get(param);
}

const startCategory = getQueryParam('category');


function homePage() {
  window.location.href = "index.html";
}

const map = new mapboxgl.Map({
  container: 'map',
  style: 'mapbox://styles/mapbox/streets-v12',
  center: [-73.996, 40.741], // Roughly centered on Central Park, Manhattan
  zoom: 12.3,
  pitch: 30, // Tilted view for 3D buildings to visually work (i.e. not top down view)
  bearing: -20.6, // Rotation control 
  antialias: true,
  // Disable built in MapBox drag rotate so user has to use defined controller (hopefully no more stephen playtime with touchpad zoom)
  pitchWithRotate: false, 
  dragRotate: false,
  scrollZoom: false,      
  boxZoom: false,      
  doubleClickZoom: false, 
  touchZoomRotate: false,
});

let mapReady = false;
let heatmapReady = false;

let startTime = performance.now(); // Start tracking time

// This function hides the loading animation only when both the heatmap and the mapbox map are ready.
function tryHideLoader() {
  if (mapReady && heatmapReady) {
    const loaderEl = document.getElementById('loader');
    const mapEl = document.getElementById('map');

    // Fade in the map and fadeout the loader when both heatmap and mapbox map are ready.
    mapEl.style.opacity = '1';
    loaderEl.style.opacity = '0';

    // Applies the CSS class hidden to the loader once the conditions are met.
    setTimeout(() => {
      loaderEl.classList.add('hidden'); 
    }, 300); 
  }
} 

map.on('load', () => {
  let endTime = performance.now();
  let loadTime = endTime - startTime;

// Add 3D Buildings layer

/* zoom level of 3D buildings limited by data provided by MapBox for 3D visualization 
i.e. we can't change it so that 3D buildings will appear at greater zoom levels */

map.addLayer({
'id': '3d-buildings',
'source': 'composite',
'source-layer': 'building',
'filter': ['==', 'extrude', 'true'],
'type': 'fill-extrusion',
'minzoom': 15, 
'paint': {
    'fill-extrusion-color': 'rgba(236, 236, 236, 1)',
    'fill-extrusion-height': [
    'interpolate', ['linear'], ['zoom'],
    15, 0,
    15.5, ['get', 'height'] // This is for preloading 3D buildings so that they appear "smoothly"
    ],
    'fill-extrusion-base': [
    'interpolate', ['linear'], ['zoom'],
    15, 0,
    15.5, ['get', 'min_height']
    ],
    'fill-extrusion-opacity': .9
}
});

// Setting light source to try improve 3D buildings visually
map.setLight({
  anchor: 'viewport',
  color: 'rgba(252, 246, 218, 1)',
  intensity: 0.4,
  position: [1, 80, 100] // degree of light, sun angle, distance
});

map.once('idle', () => {
mapReady = true;
tryHideLoader();
});
});

    let fullHeatmapData = null;

    const currentHour = new Date().getHours();
    const displayHour = currentHour === 0 ? 23 : currentHour;
    let selectedHour = displayHour;

    document.getElementById('hourSlider').value = displayHour;
    document.getElementById('hourDisplay').textContent = displayHour;

    const today = new Date();
    const dayLabels = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];

    function formatDate(d) {
    return d.toISOString().split('T')[0];
    }

    // This is a new chunk accumulation function
    let dataBatch = []; // Stores chunks

    function accumulateAndRenderData(chunk) {
    dataBatch.push(chunk);

    // If the batch has 10 chunks, combine them and render to the map
    if (dataBatch.length > 10) {
        const combinedData = {
        type: "FeatureCollection",
        features: dataBatch.flat()
        };

        // Update the heatmap with the combined data
        map.getSource('heatmap').setData(combinedData);

        // Clear the batch for the next set of data
        dataBatch = [];
    }
    }

function loadHeatmapForDate(dateStr) {
  document.getElementById('loader').style.display = 'block';
  heatmapReady = false;

  fetch(`/api/zone_predictions?date=${dateStr}`)
    .then(res => {
      if (!res.ok) {
	throw new Error(`Http error. Status: ${res.statusText} - ${res.statusText}`);
      }

      return  res.json();
    })
    .then(data => {
      fullHeatmapData = data;

      const startRenderTime = performance.now();

      accumulateAndRenderData(data.features);

      const initialFiltered = {
        type: "FeatureCollection",
        features: data.features.filter(f => f.properties.hour === selectedHour)
      };

      if (map.getSource('heatmap')) {
        map.getSource('heatmap').setData(initialFiltered);
      } else {
        map.addSource('heatmap', {
          type: 'geojson',
          data: initialFiltered
        });

        map.addLayer({
          id: "heatmap-layer",
          type: "heatmap",
          source: "heatmap",
          paint: {
            "heatmap-weight": [
              "interpolate", ["linear"], ["get", "intensity"],
              0, 0,
              1, 1
            ],
            "heatmap-intensity": [
              "interpolate", ["linear"], ["zoom"],
              12, 0.40,
              13, 1,
              14, 1.1,
              15, 1.7,
              16, 2.5
            ],
            "heatmap-color": [
              "interpolate", ["linear"], ["heatmap-density"],
              0, "rgba(0, 0, 255, 0)",
              0.1, "royalblue",
              0.2, "deepskyblue",
              0.4, "cyan",
              0.6, "lime",
              0.8, "yellow",
              1, "red"
            ],
            "heatmap-radius": [
              "interpolate", ["linear"], ["zoom"],
              12, 45,
              13, 60,
              14, 110,
              15, 180,
              16, 300
            ],
            "heatmap-opacity": [
              "interpolate", ["linear"], ["zoom"],
              12, 0.45,
              14, 0.4,
              16, 0.35 // How opaque the map appears (slightly lower on full-zoom when 3D buildings appear)
            ]
          }
        });
        
        // Hopefully make 3D layer move on top of heatmap (not under)
        map.moveLayer('3d-buildings');

        map.once('idle', () => {
          heatmapReady = true;
          tryHideLoader();
        });
      }
    });
}


// Creates button for the seven days. Clicking on each button will call the endpoint for that date and display the data.
document.addEventListener('DOMContentLoaded', () => {
    const daysContainer = document.getElementById('dayButtons');
    daysContainer.innerHTML = '';
    // The today value has already been defined as today's date. This loop sets for date for each newly created button as today + i.
    // This results in each button being the next date.
    for (let i = 0; i < 7; i++) {
        const btnDate = new Date(today);
        btnDate.setDate(today.getDate() + i);

        const dateStr = formatDate(btnDate);
        const weekday = btnDate.getDay();
        const btn = document.createElement('button');

        btn.textContent = (i === 0) ? 'Today' : dayLabels[weekday];
        btn.dataset.date = dateStr;

        if (i === 0) btn.classList.add('active');

        btn.addEventListener('click', () => {
        document.querySelectorAll('#dayButtons button').forEach(b => b.classList.remove('active'));
        btn.classList.add('active');
        loadHeatmapForDate(dateStr);
        });

        daysContainer.appendChild(btn);
    }
});

/* --------- FIRST LOAD (today) --------- */
loadHeatmapForDate(formatDate(today));

// Appends hour values below slider, following slider interval distance for hours 
const tickLabels = document.getElementById('tickLabels');

for (let i = 0; i <= 23; i++) {
const label = document.createElement('span');
label.textContent = i;
tickLabels.appendChild(label);
}

// Hour slider on event hour change
document.getElementById('hourSlider').addEventListener('input', () => {
selectedHour = parseInt(document.getElementById('hourSlider').value); // Change selected hour to time slider selection
document.getElementById('hourDisplay').textContent = selectedHour;
updateHeatmap();
});

// Combined day and hour filter function
function updateHeatmap() {
if (fullHeatmapData && map.getSource('heatmap')) {
    const filtered = {
    type: "FeatureCollection",
    features: fullHeatmapData.features.filter(f =>
        f.properties.hour === selectedHour
    )
    };
    map.getSource('heatmap').setData(filtered);
}
}

  // Get element for search input and suggestions drop down
  const searchInput = document.getElementById('searchInput');
  const suggestionsList = document.getElementById('suggestions');

  // Manhattan bounding box to restrict points to only Manhattan (i.e heatmap only appears over manhattan)
  const manhattanBBox = [-74.03, 40.70, -73.93, 40.88]; 

  // Input for search bar and simultaneous zoom to selected location, I need to make custom mag controls as user gets stuck on zoom-in without use of scroll-wheel etc.
  searchInput.addEventListener('input', () => {
    const query = searchInput.value.trim();
    if (query.length > 0) {
      const endpoint = `https://api.mapbox.com/geocoding/v5/mapbox.places/${encodeURIComponent(query)}.json?access_token=${mapboxgl.accessToken}&autocomplete=true&bbox=${manhattanBBox.join(',')}&limit=5`;

      fetch(endpoint)     
        .then(response => response.json())
        .then(data => {
          suggestionsList.innerHTML = ''; // Clear previous suggestions
          if (data.features && data.features.length > 0) {
            data.features.forEach(feature => {
              const li = document.createElement('li');
              li.textContent = feature.place_name;
              li.addEventListener('click', () => {
                const [lng, lat] = feature.center;
                // Move map to location
                map.flyTo({
                  center: [lng, lat],
                  zoom: 16
                });
                // Clear suggestions and input
                suggestionsList.innerHTML = '';
                searchInput.value = feature.place_name;
              });
              suggestionsList.appendChild(li);
            });
          } else {
            const li = document.createElement('li');
            li.textContent = 'No results found';
            suggestionsList.appendChild(li);
          }
        })
        .catch(error => {
          console.error('Error fetching suggestions:', error);
        });
    } else {
      suggestionsList.innerHTML = ''; // Clear if input is empty
    }
  });

  // Zoom functionality
  document.getElementById('zoomIn').addEventListener('click', () => {
    const currentZoom = map.getZoom();
    const newZoom = Math.min(16, currentZoom + 1); // Prevent zooming in too far beyond heatmap radius
    map.flyTo({
      zoom: newZoom,
      essential: true
    });
  });

  document.getElementById('zoomOut').addEventListener('click', () => {
    const currentZoom = map.getZoom();
    const newZoom = Math.max(12.3, currentZoom - 1); // Prevent zooming out beyond Manhattan area (i.e. visually incomprehensible)
    map.flyTo({
      zoom: newZoom,
      essential: true
    });
  });

  document.getElementById('recenter').addEventListener('click', () => {
    map.flyTo({
      center: [-73.996, 40.741], // On recenter, POV resets to center view and default zoom (i.e. default view)
      zoom: 12.3,
      pitch: 30, 
      bearing: -20.6,
    });
    // On-click, resets thumb position of pitch & bearing controller, in-case control had been used
    thumb.style.left = '35px';
    thumb.style.top = '35px';
  });
  
  // Hide suggestions on click outside of search
  document.addEventListener('click', (e) => {
    if (!searchInput.contains(e.target) && !suggestionsList.contains(e.target)) {
      suggestionsList.innerHTML = '';
    }
  });

  // Clear input if it's clicked again and not empty (user does not need to manually delete prior text entry now)
  searchInput.addEventListener('click', () => {
    if (searchInput.value !== '') {
      searchInput.value = '';
      suggestionsList.innerHTML = ''; 
    }
  });

// Marker generation and Favourite system	  
let geojsonData;
	  
fetch(`api/poi_data`) 
  .then(response => response.json())
  .then(data => {

    geojsonData = data;

    // Attractions page category query selection
    if (startCategory) {
      showCategoryMarkers(startCategory); 
      activeCategory = startCategory;

      const matchingBtn = document.querySelector(`[data-filter="${startCategory}"]`);
      if (matchingBtn) matchingBtn.classList.add('active');
    }
  });

/*
 Main complexity; as marker generation is dynamic on-click for filter buttons, markers need to be dynamically generated everytime
 through favourite system. However, this allows scalability where one simply needs to add category.png when a new category is added to map, then
 marker can be dynamically generated instantly. In tandem with ease of addition of attraction (category) to Attractions page, this complexity
 was necessary to fit with ethos of app (i.e. easy scalability).
 */

let markers = []; // Store generic markers from filter buttons (category selection)
let activeCategory = null;
let favourites = []; // Stores favourited marker info such as co-ords, id
let favouriteButtons = {}; // Stores favourite buttons element of marker with key (id) for toggling on/off
let favouriteMarkers = []; // Stores marker property info for marker generation, I found seperating fav marker on map and fav marker's info in favourite list best to control interactions
let favouritesVisible = false; // Used to hide fav markers, again, keeping generic marker visibility control (with "clearMarkers()") seperate from favMarkers allows control over what appears and when

function addFavourite(markerData) {
  const exists = favourites.find(fav => fav.id === markerData.id);
  if (!exists) {
    favourites.push(markerData);
    localStorage.setItem('favourites', JSON.stringify(favourites)); // Store in localStorage
    updateFavouritesDropdown();
  }
}

// Helper function to match category to property for marker icon generation etc.
function matchCategory(marker) {
  if (marker.category || !geojsonData) return marker;
  // Avoids duplicate markers from being added based on matching coordinates
  const match = geojsonData.features.find(f =>
    JSON.stringify(f.geometry.coordinates) === JSON.stringify(marker.coordinates)
  );
  if (!match) return marker;
  // Matching property to category, then assigning marker with that category (this is then used for icon assignment etc.)
  const props = match.properties;
  if (props.tourism === 'gallery') marker.category = 'gallery';
  else if (props.tourism === 'museum') marker.category = 'museum';
  else if (props.leisure === 'park') marker.category = 'park';
  else if (props.amenity === 'restaurant') marker.category = 'restaurant';
  else if (['clothes', 'supermarket', 'department_store', 'mall', 'convenience'].includes(props.shop)) {
    marker.category = 'shop';
  }

  return marker;
}


// Uses id to uniquely identify marker for removal 
function removeFavourite(id) {
  // Remove favourite from array then update favourites
  const stringId = id;
  favourites = favourites.filter(fav => fav.id !== stringId);
  localStorage.setItem('favourites', JSON.stringify(favourites)); // Store in localStorage
  updateFavouritesDropdown();
   
  // Update favouritebutton (in popup) to white star (empty body star)
  if (favouriteButtons[Array.isArray(id) ? id.join(',') : id]) {
  favouriteButtons[Array.isArray(id) ? id.join(',') : id].textContent = '☆';
  }
    // Remove marker from map
  const favMarkerObj = favouriteMarkers.find(fm => fm.id === (Array.isArray(id) ? id.join(',') : id));;
  if (favMarkerObj) {
    favMarkerObj.marker.remove();
    favouriteMarkers = favouriteMarkers.filter(fm => fm.id !== stringId);
  }
}

// When the user accesses the web app this function will load the previously stored favourites.
function loadFavouritesFromStorage() {
  const storedFavourites = JSON.parse(localStorage.getItem('favourites'));
  if (storedFavourites && Array.isArray(storedFavourites)) {
    favourites = storedFavourites;
    updateFavouritesDropdown(); 
  }
}

// Called on page load to initialize the stored favourites.
document.addEventListener('DOMContentLoaded', loadFavouritesFromStorage);

// Dynamically adding items to 'favouritesList' i.e. favourite markers
function updateFavouritesDropdown() {

  const list = document.getElementById('favouritesList');
  list.innerHTML = '';

  // Updating favourites list dynamically for favourited marker
  favourites.forEach(marker => {

    // Find marker category using helper function
    matchCategory(marker);

    // Create list item with bootstrap
    const listItem = document.createElement('li');
    listItem.className = 'dropdown-item d-flex justify-content-between align-items-center';
    
    // Add marker name to item (unnamed as error handling)
    const nameSpan = document.createElement('span');
    nameSpan.textContent = marker.name || 'Unnamed';
    nameSpan.style.cursor = 'pointer';

    // Add on-click to marker name, which goes to coords and zooms in
    nameSpan.addEventListener('click', () => {
      map.flyTo({ center: marker.coordinates, zoom: 16 });

      // Custom marker icon based on category 
      const el = document.createElement('div');
      el.className = 'custom-marker';
      const iconCategory = marker.category || 'favourite'; // Favourite in case category somehow not matched (it should inherently)
      el.innerHTML = `<img src="media/${iconCategory}.png" alt="${iconCategory} icon" class="marker-icon">`; // Same principle as marker generation, category matches .png

      // Popup is necessary for marker to have on-click div with favourite button inside (plus name of marker)
      const popupContent = document.createElement('div');
      popupContent.className = 'popup-content';

      const title = document.createElement('div');
      title.textContent = marker.name || 'Unnamed'; // Unnamed in case name not available property for possible error handling
      title.style.fontWeight = 'bold'; // Just adding styling here, as it will only apply for list item
      popupContent.appendChild(title);

      // Paragraph in popup
      const favTxt = document.createElement('div');
      favTxt.className = 'favTxt'
      favTxt.textContent = 'Click to add to your Favourites!';
      popupContent.appendChild(favTxt);

      const markerId = marker.coordinates.join(',');


      const favBtn = document.createElement('button');
      const isFav = favourites.find(fav => fav.id === markerId);    
      favBtn.textContent = isFav ? '★' : '☆';
      favBtn.className = 'favourite-button'; // favourite-button is re-used so className for css to apply
      
      // On-click favBtn stopProp stops click from possibly affecting other items 
      favBtn.addEventListener('click', (e) => {
        e.stopPropagation();
        
        // Checking if favourite button on-click associated marker is already favourited, removes or adds if not
        const alreadyFavourited = favourites.find(fav => fav.id === markerId);
        if (!alreadyFavourited) {
          favBtn.textContent = '★'; 
          favourites.push({
            id: markerId,
            name: marker.name,
            coordinates: marker.coordinates,
            category: marker.category
          });
            try {
              const serialised = JSON.stringify(favourites);
              localStorage.setItem('favourites', serialised); // Saves updated list to the local storage. 
            } catch (err) {
              console.error('Error saving to localStorage:', err);
            }
            updateFavouritesDropdown(); // Function call to update favourite dropdown meny with the stored favourites. 
        } else {
          removeFavourite(markerId); // Remove from local storage. 
          favBtn.textContent = '☆';
        }
      });

      popupContent.appendChild(favBtn);

      // Marker is now constructed 
      const markerObj = new mapboxgl.Marker(el)
        .setLngLat(marker.coordinates)
        .setPopup(new mapboxgl.Popup().setDOMContent(popupContent))
        .addTo(map);

      markers.push(markerObj); 
    });
    
    // Adds a small red 'x' bootstrap button to each favourited location in dropdown list
    // on-click, removes favourite from list 
    const removeBtn = document.createElement('button');
    removeBtn.className = 'btn btn-sm btn-outline-danger ms-2';
    removeBtn.innerHTML = '&times;';
    removeBtn.addEventListener('click', (e) => {
      e.stopPropagation();
      removeFavourite(marker.id);
    });

    listItem.appendChild(nameSpan);
    listItem.appendChild(removeBtn);
    list.appendChild(listItem);
  });
}

// Function to clear existing markers
function clearMarkers() {
  markers.forEach(marker => marker.remove());
  markers = [];
}

function toggleFavouritesMarkers() {
  if (favouritesVisible) {
    favouriteMarkers.forEach(marker => marker.remove());
    favouriteMarkers = [];
    favouritesVisible = false;
    // There are scenarios when on-click item in list, marker will appear persistently even after
    // even after favourites button clicked again (i.e. toggle should occur).
    // adding clearMarkers makes sure markers are removed regardless.
    // Basically, in tandem with clearMarkers, there should be no scenario where marker does not disappear now.
    clearMarkers();
    // I have updated so that when Favourites is clicked, active is removed from filter-button, thus removing styling from last active button 
    // This is to stop scenario where User clicks Fav Button, previous filter-button category was still visually active, but its markers were hidden due to Fav Button being active
    // However, Favourite button is still only way to hide Favourite markers (i.e. filter-button and Favourite button markers can be visible at same time, 
    // if User selects a category with filter-button while Fav list is open)
    document.querySelectorAll('.filter-button').forEach(btn => {
      btn.classList.remove('active');
    });
    activeCategory = null;
    // Basically, on Fav toggle on or off, filter-button active is removed, and active category set to null (so filter-buttons are now fully reset basically)
  } else {
    document.querySelectorAll('.filter-button').forEach(btn => {
      btn.classList.remove('active');
    });
    activeCategory = null;
    clearMarkers(); // Clicking favourite dropdown button hides all other markers
    favourites.forEach(markerData => {
      matchCategory(markerData);

      // Create custom marker element
      const el = document.createElement('div');
      el.className = 'custom-marker';
      el.innerHTML = `<img src="media/${markerData.category}.png" alt="${markerData.category} icon" class="marker-icon">`;

      // Build popup content again
      const popupContent = document.createElement('div');
      popupContent.className = 'popup-content';

      const title = document.createElement('div');
      title.textContent = markerData.name || 'Unnamed';
      popupContent.appendChild(title);

      // Paragraph in popup
      const favTxt = document.createElement('div');
      favTxt.className = 'favTxt'
      favTxt.textContent = 'Click to add to your Favourites!';
      popupContent.appendChild(favTxt);
      
      // Marker generated with black star (full body star)
      const favBtn = document.createElement('button');
      favBtn.textContent = '★';
      favBtn.className = 'favourite-button';
   
      // If favourite button in popup clicked, remove and change to empty star
      favBtn.addEventListener('click', (e) => {
        e.stopPropagation();
        removeFavourite(markerData.id);
        favBtn.textContent = '☆';

          try {
          const serialised = JSON.stringify(favourites);
          localStorage.setItem('favourites', serialised);
        } catch (err) {
          console.error('Error saving after removal:', err);
        } 
      });

      popupContent.appendChild(favBtn);

      const markerPopup = new mapboxgl.Popup().setDOMContent(popupContent);

      const marker = new mapboxgl.Marker(el)
        .setLngLat(markerData.coordinates)
        .setPopup(markerPopup)
        .addTo(map);

      favouriteMarkers.push(marker);
    });

    favouritesVisible = true;
  }
}

// Function to show markers for a given category
function showCategoryMarkers(category) {
  if (!geojsonData) return;

  clearMarkers();

  geojsonData.features.forEach(feature => {
    const props = feature.properties;
    const coords = feature.geometry.coordinates;

    // Match category to data (further categories can easily be implemented following syntax)
    if (
      (category === 'gallery' && props.tourism === 'gallery') ||
      (category === 'museum' && props.tourism === 'museum') ||
      (category === 'park' && props.leisure === 'park') ||
      (category === 'restaurant' && props.amenity === 'restaurant') ||
      // Combined shop filter, there are other types such as 'alcohol' etc. however, I am adding the standard "shop" a user might expect.
      (category === 'shop' && (
        props.shop === 'clothes' ||
        props.shop === 'supermarket' ||
        props.shop === 'department_store' ||
        props.shop === 'mall' ||
        props.shop === 'convenience'
      ))
    ) {
      // Create a custom marker using the category for icon (category png has same name)
      const el = document.createElement('div');
      el.className = 'custom-marker';
      el.innerHTML = `<img src="media/${category}.png" alt="${category} icon" class="marker-icon">`;

      // Avoids scenario where markers share same id, coords are unique float numbers so no chance of accidentally removing marker if same feature.id (This was happening previously)
      const id =  feature.geometry.coordinates.join(',');
      
      // Same code logic for favourite button added to generic markers

      // Create popup content 
      const popupContent = document.createElement('div');
      popupContent.className = 'popup-content';

      // Location name
      const title = document.createElement('div');
      title.className = 'popup-title';
      title.textContent = props.name || category;
      popupContent.appendChild(title);
      
      // Paragraph in popup
      const favTxt = document.createElement('div');
      favTxt.className = 'favTxt'
      favTxt.textContent = 'Click to add to your Favourites!';
      popupContent.appendChild(favTxt)

      // Favourite button
      const favBtn = document.createElement('button');
      favBtn.textContent = favourites.find(fav => fav.id === id) ? '★' : '☆';
      favBtn.className = 'favourite-button';

      favBtn.addEventListener('click', (e) => {
        // Only favourite button will receive event
        e.stopPropagation();
        try {
    const serialised = JSON.stringify(favourites);
    localStorage.setItem('favourites', serialised);
    } catch (err) {
}

        const stringId = coords.join(','); 
        const alreadyFavourited = favourites.find(fav => fav.id === stringId);

        if (!alreadyFavourited) {
          favBtn.textContent = '★';
          const favData = {
            id: stringId,
            name: props.name || 'Unnamed',
            coordinates: coords,
            category
          };

          favourites.push(favData);

          favouriteButtons[stringId] = favBtn;

        try {
          const serialised = JSON.stringify(favourites);
          localStorage.setItem('favourites', serialised); // Save to local storage    
        } catch (err) {
          console.error('Error saving to localStorage:', err);
        }

          updateFavouritesDropdown(); // Update favourites UI.
        } else {
          removeFavourite(stringId); // Remove from favourites array and local storage. 
          favBtn.textContent = '☆';
        }
      });
       
      // Favourite button added to pop-up
      popupContent.appendChild(favBtn);

      // Create popup with content
      const popup = new mapboxgl.Popup().setDOMContent(popupContent);

      // Add marker (Which contains the popup with the favourite button attached)
      const marker = new mapboxgl.Marker(el)
        .setLngLat(coords)
        .setPopup(popup)
        .addTo(map);

      markers.push(marker);

    }
  });
}

// Listener added for toggling favourites markers on favourite button click
document.getElementById('favouritesButton').addEventListener('click', toggleFavouritesMarkers);

// Should clear category from URL in instance where User uses Attractions page to select Category
// Thus stopping instacne where Attraction remains persistent through page reload, even if Category is deselected with filter-button
function clearCategoryFromURL() {
  const url = new URL(window.location);
  url.searchParams.delete('category'); 
  window.history.replaceState({}, '', url); 
}

// Category button marker generation
document.querySelectorAll('.filter-button').forEach(btn => {
  btn.addEventListener('click', () => {
    const category = btn.dataset.filter;

    if (activeCategory === category) {
      // Clear, deactivate, and remove "active" (to remove css)
      clearMarkers();
      activeCategory = null;
      btn.classList.remove('active');
      // Remove from URL in case selected through Attractions
      clearCategoryFromURL();
    } else {
      clearCategoryFromURL();
      // Show selected category
      showCategoryMarkers(category);
      activeCategory = category;
      
      // add active to active button for css (removed if double-clicked)
      document.querySelectorAll('.filter-button').forEach(b => b.classList.remove('active'));
      btn.classList.add('active');
    }
  });
});

// Map pitch and bearing controller 

const container = document.getElementById('joystick-container');
const thumb = document.getElementById('joystick-thumb');
const center = { x: 50, y: 50 }; // Joystick assigned to center of container as 100x100, x, y = 50 is centered

// Update the x and y coords depending on their movement, equally calculate relative movement to assign new bearing and pitch
function updateJoystick(x, y) {

  // const are equal to difference between initial center x,y values and new x, y value
  const dx = x - center.x;
  const dy = y - center.y;

  // Using pythagorean theorem to find straight line distance pointer has moved from center
  const distance = Math.min(Math.sqrt(dx * dx + dy * dy), 40);

  // Finding the angle (using atan2) of the direction the thumb has moved from the center
  // This is necessary as angle of trajectory is combined with distance moved is needed to plot final thumb position
  const angle = Math.atan2(dy, dx);

  // Angle and distance moved from center provides polar coordinates, but x, y values are necessary for positioning
  // Basically, polar coordinates (distance from center, angle) now need to be converted to Cartesian (x, y) coordinates for thumb position

  // The following is calculating the Cartesian x, y from the Polar coordinates
  // -15 is to offset the fact the thumb will be positioned not at its center, but at its corner, the thumb is 30x30 so 15 is offsetting by its radius
  // Thus this gives the actual position based on the centre of the thumb

  // Math.cos(angle) gives the x-axis (horizontal) component
  const thumbX = center.x + distance * Math.cos(angle) - 15;

  // Math.sin(angle) gives the y-axis (vertical) component 
  const thumbY = center.y + distance * Math.sin(angle) - 15;

  // Thumb style is then updated with the result of previous calculation, so thumb is now located correctly in container relative to what user moved it
  thumb.style.left = `${thumbX}px`;
  thumb.style.top = `${thumbY}px`;

  // Taking joystick movement for camera changes
  const camX = dx;
  const camY = dy;

  const sensitivity = 0.04; // Sets sensitivity of control, i.e. higher sens causes greater change  
  

  // New bearing is calculated using mapbox getBearing and incrementing it by sensitivity multiplied by x axis value
  const newBearing = map.getBearing() + camX * sensitivity;
  // Similarly pitch is calculate using same principle, however, pitch is restricted with min and max to stop user from selecting bad visual perspectives
  const newPitch = Math.max(20, Math.min(75, map.getPitch() - camY * sensitivity));

  // Whether result of camX/camY * sensitivity is subtracted or added to relevant pitch/bearing, controls which way the map moves on joystick movement
  // I've set it so that moving right, rotates map right. Moving up, moves increases pitch.

  // Map is then updated with newBearing and newPitch
  map.rotateTo(newBearing, { duration: 0 });
  map.setPitch(newPitch);
}

function moveThumb(e) {
  // Getting container size relative to page (i.e joystick-container)
  const rect = container.getBoundingClientRect();
  // As stated earlier x and y based off corner and top, and their relative position
  // Why -15 is necessary as center of joystick should be logical center of calculation
  const x = e.clientX - rect.left;
  const y = e.clientY - rect.top;
  // Calculated x and y pushed to updateJoystick 
  updateJoystick(x, y);
}

// releaseThumb to control how joystick and user interaction is listened to
function releaseThumb() {
  // When pointer is released, stops listening
  container.removeEventListener('pointermove', moveThumb);
  container.removeEventListener('pointerup', releaseThumb);
}

// When pointerdown (User presses joystick thumb) eventlistener tracks
container.addEventListener('pointerdown', (e) => {
  // Pointer events will continue to update even if pointer leaves container
  // Continues to call moveThumb function until pointerup is heard (i.e. user stops pressing joystick thumb)
  container.setPointerCapture(e.pointerId);
  moveThumb(e); 
  container.addEventListener('pointermove', moveThumb);
  container.addEventListener('pointerup', releaseThumb);
});


// Customized bootstrap, to stop outside click of Favourites button from dropdown menu causing close
// Necessary as otherwise sequence of toggling favourites on map is messed up 
// Necessary as if user misclicks (easy to do) while trying to use UI (day-selection, time slider, zoom cons, 3D con) would be a constant problem.

const favouritesButton = document.getElementById('favouritesButton');
const favouritesList = document.getElementById('favouritesList');

favouritesButton.addEventListener('click', (e) => {
  e.stopPropagation(); // Prevent outside click listener from immediately closing it, stops sequence change
  favouritesList.classList.toggle('show');
});

// This code checks local storage to see if the key 'tutorialshown' is present.
document.addEventListener('DOMContentLoaded', () => {
  if (!localStorage.getItem('tutorialShown')) {
    // If tutorialshown is not present it makes the toastDiv visible as is is not in local storage meaning the user has never seen it before.
    document.getElementById('toastDiv').hidden = false;

    // On click of the toastCloseBtn in html the toastDiv is hidden (the Popup) and the key in local storage is updated to true.
    // As a result when the user accesses the site a second time the popup will not appear.
    document.getElementById('toastCloseBtn').addEventListener('click', () => {
      document.getElementById('toastDiv').hidden = true;
      localStorage.setItem('tutorialShown', 'true');
    });
  }
});