# Code Alchemist — Feature Tracker

## Infrastructure

| Feature | Status | Evidence |
|---------|--------|----------|
| Standalone GitHub repo | VERIFIED | https://github.com/Thepluscode/code-alchemist |
| Railway project (code-alchemist) | VERIFIED | ba256bdb-5845-4351-bbbf-a8608fbd2154 |
| Backend deployed | VERIFIED | https://backend-production-5a49.up.railway.app/actuator/health → UP |
| Frontend deployed | VERIFIED | https://frontend-production-41ec.up.railway.app/ → 200 |
| Postgres provisioned | VERIFIED | Internal connection confirmed via backend health |
| Removed from CyberGuard | VERIFIED | Commit a3b93e0 — 69 files, 3950 deletions |
| CyberGuard Railway service deleted | VERIFIED | serviceDelete mutation returned true |

## Backend API

| Endpoint | Status | Evidence |
|----------|--------|----------|
| GET /api/v1/projects | VERIFIED | Returns [] on fresh DB |
| POST /api/v1/projects | VERIFIED | Created project 2c8b921e successfully |
| POST /files/inline | DEPLOYED | Had jsonb type mismatch — fix deployed, awaiting retest |
| POST /files/upload | DEPLOYED | Not yet tested |
| POST /pipeline/start | DEPLOYED | Not yet tested (needs files + ANTHROPIC_API_KEY) |
| POST /pipeline/resume | DEPLOYED | Not yet tested |
| GET /pipeline/status | DEPLOYED | Not yet tested |
| GET /rules | DEPLOYED | Not yet tested |
| PATCH /rules/:id/approve | DEPLOYED | Not yet tested |
| PATCH /rules/:id/reject | DEPLOYED | Not yet tested |
| PATCH /rules/approve-all | DEPLOYED | Not yet tested |
| GET /graph | DEPLOYED | Not yet tested |
| GET /artifacts | DEPLOYED | Not yet tested |
| GET /audit | DEPLOYED | Not yet tested |

## Frontend Pages

| Page | Status | Evidence |
|------|--------|----------|
| / (redirect to /projects) | VERIFIED | HTML contains redirect logic |
| /projects (list) | VERIFIED | Page renders with title "Code Alchemist" |
| /projects/:id (detail) | DEPLOYED | Not yet tested with real project |

## AI Pipeline (requires ANTHROPIC_API_KEY)

| Step | Status | Notes |
|------|--------|-------|
| Ingestion | DEPLOYED | Parses uploaded files, detects language |
| Rule Extraction | DEPLOYED | Needs Claude API key |
| Graph Building | DEPLOYED | Needs Claude API key |
| Approval Gate | DEPLOYED | Human-in-the-loop rule approval |
| Architecture Design | DEPLOYED | Needs Claude API key |
| Code Generation | DEPLOYED | Needs Claude API key |
| Validation | DEPLOYED | Needs Claude API key |
| Migration Planning | DEPLOYED | Needs Claude API key |

## Pending

- [ ] Set ANTHROPIC_API_KEY on Railway backend service
- [ ] Retest /files/inline after jsonb fix deploy
- [ ] Test full pipeline end-to-end
- [ ] Rename Java package from com.theplus.cyberguard.alchemist to com.theplus.codealchemist
- [ ] Lock down CORS to frontend domain only
