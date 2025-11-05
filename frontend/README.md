# Frontend — GitHub Org Snapshot (React + Vite)

## Setup
```bash
cd frontend
cp .env.example .env
npm install
npm run dev
```

Open http://localhost:5173

## Configure backend URL
- Set `VITE_API_BASE_URL` in `.env` (e.g., `http://localhost:8080`).
- If unset, the app uses the same origin as the frontend.

## Build
```bash
npm run build
npm run preview
```
