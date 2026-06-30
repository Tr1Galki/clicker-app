# Docker Integration Design

## Overview

Add Docker support to the clicker app for two purposes:

1. **Local development** — `docker compose up` starts both services with one command
2. **Railway deploy** — `backend/Dockerfile` gives explicit control over the build environment

## Scope

- `backend/Dockerfile` — multistage build for the Spring Boot backend
- `docker-compose.yml` — local development orchestration

Frontend (Vercel) is not touched — Vercel builds Next.js natively without Docker.

## backend/Dockerfile

Multistage build:

**Stage `build`** (`maven:3.9-eclipse-temurin-21-alpine`):
- Copies `pom.xml` and `src/`
- Runs `mvn package -DskipTests` to produce the JAR

**Stage `run`** (`eclipse-temurin:21-jre-alpine`):
- Copies the JAR from the build stage
- Exposes port `8080`
- Entrypoint: `java -jar app.jar`

Smaller final image (JRE only, no Maven/JDK).

Railway picks up `backend/Dockerfile` automatically on the next push and uses it instead of its own Maven detection.

## docker-compose.yml

Located at `v0-test/docker-compose.yml`.

Two services:

**`backend`**:
- Build context: `./backend`
- Port: `8080:8080`

**`frontend`**:
- No Dockerfile — runs `npm run dev` directly
- Working dir: `./frontend`
- Port: `3000:3000`
- Env: `NEXT_PUBLIC_API_URL=http://localhost:8080`
- Depends on: `backend`

The frontend uses `localhost:8080` because `NEXT_PUBLIC_API_URL` runs in the browser,
which cannot reach Docker's internal network. Port `8080` is forwarded to the host,
so the browser reaches the backend directly.

## Deploy Flow (after this change)

```
git push → GitHub
  ├── Railway: sees backend/Dockerfile → docker build → docker run → backend live
  └── Vercel: builds frontend/ natively → frontend live (NEXT_PUBLIC_API_URL from Vercel dashboard)
```

## Local Dev Flow

```bash
docker compose up   # starts backend (Docker) + frontend (npm run dev)
# backend:  http://localhost:8080
# frontend: http://localhost:3000
```

## Success Criteria

- `docker build -t clicker-backend ./backend` succeeds
- `docker compose up` starts both services
- Frontend at `localhost:3000` can click and see count increment
- Railway redeploys successfully from `backend/Dockerfile` on next push
