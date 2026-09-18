# CrowdScope (Frontend)



This is my personal fork of the [CrowdScope project](https://github.com/smc94724/COMP47360-Research-Practicum---Team-7), a research practicum at University College Dublin. I served as **Frontend Lead** on the five-member team, where my primary responsibilities included UI/UX design and implementation, the creation of the frontend code as a whole with the intention of modularity for ease of replication on further locations, and general testing, bug fixing, and page responsiveness.

**Live Deployment**: Currently in the process of updating backend and data to get it live again, watch this space!

---

## My Contributions

* **The Frontend Code is modular**: Code has been designed so that with updated data and simply changing latitude and longitude in Mapbox, CrowdScope can be used for any geographical area.
* **All UI / UX Implemented**: Designed and implemented the UI and UX for the website as seen [here](#examples-of-homepage-map-ui), where accessibility was a key concern.
* **Map / data visualization**: I chose a 3D heatmap through Mapbox, as I found this to be the best map based API to achieve the goal of clear busyness visualization on a map.
* **API integration**: Mapbox is a very versatile API, allowing great amounts of fine tuning to produce a polished end product.
* **Performance / optimization**: Optimization was particularly evident in data display within the heatmap, where heatspots need to be a specific density over a set distance to achieve full map coverage and avoid data bald spots.
* **Additional features**: The page employs multiple features to allow total user control over the map, including joysticks and intentionally restricted zoom fuinctionalities to gurantee consistenet user experience.
* **General problem solving**: I had a clear idea for this project, but we were met with multiple bumps in the process, I contributed to other areas of the project through ideation where we ultimately found suitbale solutions, which led to our project working exactly as intended by completion of out sprints. 

---

## Tech Highlights

* **Frontend framework** had been designed with modularity in mind, this is because I wanted to make it with the future in mind, where we could reapply the same code with minor tweaks and use it on any geographic area (Watch this space!).
* **Language stack** was primarily hand coded in the standard webstack (no react etc.) as I wanted total control over how the UI and UX would look and interact, this was more time consuming, but meant our end product guaranteed consistent interaction.
* **Mapbox** as stated was chosen over other map API's as it delivered a key feature in a 3D layer on zoom in, which further increased its usability for our purpose of clearly outlining crowd levels within cities etc.
* **Styling framework / library** as referenced, styling was entirely by hand without the use of libraries etc. styling was kept minimal as I felt the heatmap and visual understanding of the user would be compromised in the scenario that the styling was distracting or would lead to visual overwhelm. A demo of the page can be seen [here](#examples-of-homepage-map-ui)!
* **Day and Time Features** beyond controls, the map uses predictive data for future days, and current data for "Today", where upon user selection of a day, the map updates relative to the day and time slider to show predicted busyness levels.
* **Markers and Favouriting Features** the map dynamically generates information for each location based off of a dataset of locations which contains location type etc. dynamic marker generation reduces the loadtimes for the page, as each location is not stored, but simply only generated on user selection (Restaurant, Park, Shop etc.). This design was made with the intention that any number of new marker types (Cinema, Carwash etc.) could be added at a later date, where simply matching the data structure would allow instant dynamic generation on the map. This returns to the design philosophy of replication and expansion of this app, with an eye towards future proofing Crowdscope. Favouriting employs a similar system where favourites are not stored as markers, but as location information, where upon selection of Favourites, markers are dynamically generated. Load times were incredibly fast for this process as well.   


---

## Examples of Homepage Map UI

https://github.com/user-attachments/assets/bb33ff8e-43c1-46a1-9469-e7b55e680cce

---
**Controls can be seen, including zoom and joystick controls, allowing user to take full advantage of Mapbox's 3D layer** 

---


<img width="1919" height="1052" alt="image" src="https://github.com/user-attachments/assets/26ac6dc1-4e96-4b43-9bcb-ce9454fcfc1c" />









