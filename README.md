# GitHub Org Snapshot

A lightweight full-stack demo app that displays **top repositories of any GitHub organization or user**.

Built as a **test assignment** — clean architecture, fully Dockerized, and production-ready structure.

---

## Features

- **Endpoints**
    - `GET /api/health` → Service health check.
    - `GET /api/org/{org}/repos?limit=5&sort=stars|updated` → Fetch top repositories.
- **Sorting** — by stars (default) or by last update date.
- **Fallback** — automatically retries `/users/{org}` if `/orgs/{org}` returns 404.
- **Caching** — GitHub responses cached for **60 seconds** (Caffeine).
- **Frontend** — modern React + Vite SPA with loading, empty, and error states.
- **DevOps** — clean Docker setup (multi-stage builds + `docker compose`).
- **Testing** — backend (JUnit 5 + WireMock) and frontend (Vitest + React Testing Library).
- **CI/CD** — ready-to-run GitHub Actions workflows for build + test (+ optional Docker build).

---

## Tech Stack

| Layer | Stack |
|-------|-------|
| **Backend** | Java 17, Spring Boot 3, WebClient, Spring Cache (Caffeine) |
| **Frontend** | React 18, Vite 5 |
| **Testing** | JUnit 5, WireMock, Vitest, React Testing Library |
| **DevOps** | Docker, docker-compose, GitHub Actions |
| **Packaging** | Multi-stage Dockerfiles, Nginx for static delivery |

---

## 🗂Repository Structure

github-org-snapshot/
├── backend/
│ ├── src/main/java/... # Controllers, services, DTOs, configs
│ ├── src/test/java/... # JUnit + WireMock tests
│ ├── pom.xml
│ └── Dockerfile
├── frontend/
│ ├── src/ # Components, styles, tests
│ ├── package.json
│ ├── vite.config.js
│ └── Dockerfile
├── .github/workflows/ # CI workflows (ci.yml, docker.yml)
├── docker-compose.yml
└── README.md



---

## Quick Start

###  Run with Docker (recommended)

```bash
docker compose up --build
```


## Then open:

### Frontend: http://localhost:5173
### Backend: http://localhost:8080/api/health

---

## Optional (to increase GitHub rate limits):

### backend:
```
cd backend
mvn spring-boot:run

# → http://localhost:8080/api/health
```

### frontend:
```
cd frontend
cp .env.example .env
npm install
npm run dev

# → http://localhost:5173
```

---

## API Reference

### Health Check
```
GET /api/health
→ 200 {"status": "OK"}
```

### Top Repositories
```
GET /api/org/{org}/repos?limit=5&sort=stars|updated
```

---

## Parametrs
| Name    | Type  | Default  | Description                  |
| ------- | ----- | -------- | ---------------------------- |
| `org`   | path  | required | Organization or username     |
| `sort`  | query | `stars`  | Sort by `stars` or `updated` |
| `limit` | query | `5`      | Number of results (1–20)     |

---

## Testing

### Backend (JUnit 5 + WireMock)

```
cd backend
mvn test
```

### Frontend (Vitest + React Testing Library)

```
cd frontend
npm test
# or:
npm run test:watch
```