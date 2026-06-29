# Clicker App Design

## Overview

A simple click counter app: the frontend sends a request on each click, the backend
counts total clicks in H2 (embedded) and returns the current count.

## Project Structure

```
neiro-test/
└── v0-test/
    ├── backend/   # Spring Boot application
    └── frontend/  # Static files from V0 prototype
```

## Backend

**Stack:** Java, Spring Boot, Spring Data JPA, H2 (embedded, in-memory)

**Data model:**

Single `Counter` entity with one row, initialized at startup:

```
counter (id BIGINT PK, count BIGINT NOT NULL)
```

**API endpoints:**

| Method | Path | Description | Response |
|--------|------|-------------|----------|
| POST | `/api/click` | Increment count by 1 | `{"count": 42}` |
| GET | `/api/count` | Get current count | `{"count": 42}` |

**CORS:** allowed for all `localhost` origins (frontend runs on a different port).

**Initialization:** on startup, insert one row with `count=0` if the table is empty.

## Frontend

Built as a V0 prototype, then saved as static HTML/CSS/JS.

- Large "Click!" button
- Counter display updates after each click
- On click: `POST /api/click` → update displayed count
- On load: `GET /api/count` → show current count

## Success Criteria

- `POST /api/click` increments count and returns new value
- `GET /api/count` returns current count
- Frontend button triggers the API and updates the UI
- App starts with `./mvnw spring-boot:run` with no external dependencies
