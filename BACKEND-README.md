## Overview

This is the backend service for our CrowdScope project. It is responsible for delivering predictive crowd density data for various Manhattan zones through a set of FastAPI endpoints. The backend connects to a PostgreSQL database, fetches geospatial data and returns the data aynchronously to the frontend where it is rendered on a mapbox map. Moreover, the backend also serves static frontend assets which are stored in the static folder.

Throughout development response times and scalability were monitored using prometheus and grafana, and these metrics were used to drive and direct development. The introduction of asynchronous data streaming, efficient querying and the optimization of REST APIs has lead to major improvement. The below data demonstrates the difference in response time and scalability between our minimum viable product (week 6) and our completed application. Major improvements can be seen in terms of both response time and scalabaility.

<img width="1920" height="1097" alt="image" src="https://github.com/user-attachments/assets/71f083ee-b1be-47df-8699-1574cc5b37df" />

## Technologies Used

### Core Stack

| Component   | Technology            | Purpose                                           |
| ----------- | --------------------- | ------------------------------------------------- |
| Programming | Python                | Core backend development                          |
| Framework   | FastAPI               | High-performance, asynchronous API server         |
| Database    | PostgreSQL            | Geospatial and tabular data storage               |
| Deployment  | Remote Linux Server   | Backend hosted on UCD provided remote server      |
| Monitoring  | Prometheus / Grafana  | Monitoring / visualizing API performance.         |


## Backend Folder Structure

```
app/
├── mobile/
├── routes/
│   ├── __init__.py 
│   ├── health.py                # Route that can be used to check if the web application is up and running.
│   ├── poi_data.py              # Route that can be caled to return data relating to places of interest that is displayed as markers on the map.
│   └── zone_predictions.py      # Fetches / filters and asynchronously returns the heatmap data to the frontend.
├── static/
├── test/
├── main.py                      # Main entrypoint file, enables prometheus monitoring and calls async_database_connection on startup 
├── async_database_connection.py # Initializes a pool of database connections. Connections be fetched dynamically.
└── __init__.py

```

## Environment Variables

Create a `.env` file in the root directory with the following variables:

```
DB_HOST=localhost
DB_PORT=5432
DB_NAME=crowdscope
DB_USER=student
DB_PASSWORD=your-db-password
```

## API Endpoints

* `GET /api/zone-predictions`
  Takes a date as a query parameter and asynchronously returns 24 hours worth of geospatial predictive data via data streaming.

* `GET /api/health`
  Health check endpoint to verify if the FastAPI application is up and running.

* `GET /api/poi_data`
  Asynchronously fetches places of interest data from the psqsl database and returns to the frontend.   

## Testing

Testing is performed using `pytest` and FastAPI's `TestClient`.

To run all tests:

```bash
pip install -r requirements.txt
pytest app/tests
```

Test coverage includes:

* API route responses
* Database connection validity
* Data structure and schema checks
* Edge case handling

---

## Running the Server Locally

Before attempting to connect locally please note that firewall and network restrictions can prevent local connection. This is especially true in the case of university or corporate wifi networks. If you are having trouble running the app locally please ensure to check your network / firewall settings and possibly try to connect to a different network. Finally, the web application is live and available on http://137.43.49.23/ if you would prefer to access the web application this way.

1. Ensure you are running all commands within the root (~) directory.

2. Install all necessary dependencies - pip install -r requirements.txt.

3. Due to firewall / network restrictions with the UCD server that contains the database it is necessary to create a tunnel from your local machine to the postgresql database on the remote server. Please ensure to leave this tunnel active at all times when working locally. - ssh -L 5432:127.0.0.1:5432 student@137.43.49.23.

4. Enter the password for this server (available upon request).

5. Then from a separate terminal - while in the project's route directory - run the following command - uvicorn app.main:app --reload. Please pay special attention to ‘INFO:app.main:Database pool initialized successfully’.

<div align="center">
  <img width="618" height="149" alt="Screenshot from 2025-07-27 14-58-51" src="https://github.com/user-attachments/assets/fb8b53c3-14a0-4189-958e-160d5f697738" />
</div>

## Maintainers

This backend was developed and is maintained by the backend lead Stephen McMahon for the **COMP47360 Research Practicum** at University College Dublin.

**Maintenance Lead**: Saniya Bhargava

---
