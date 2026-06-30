# GitHub Actions CI/CD Design

## Overview

Add a single GitHub Actions workflow that runs tests on every push/PR,
and deploys both backend and frontend on pushes to master.

## File

`.github/workflows/ci.yml`

## Triggers

- `push` to any branch
- `pull_request` to any branch

## Job: test

Runs on every push and PR.

- Runner: `ubuntu-latest`
- Java: 21 (temurin), set up via `actions/setup-java`
- Cache: Maven `.m2` cache for faster builds
- Command: `mvn test` from `backend/`
- Result: ✅/❌ shown on PR

## Job: deploy

Runs only when `github.ref == 'refs/heads/master'`, depends on `test`.

### Step 1 — Deploy backend to Railway

Uses `bervProject/railway-deploy` GitHub Action with `RAILWAY_TOKEN` secret.
Deploys the `backend/` directory.

### Step 2 — Deploy frontend to Vercel

Uses `amondnet/vercel-action` GitHub Action with:
- `VERCEL_TOKEN`
- `VERCEL_ORG_ID` = `team_MVTWK3ZoTBZ42xcXCTJTCex3`
- `VERCEL_PROJECT_ID` = `prj_BkFZ2gtuinxki0nD0d1wGPKPNQBE`
- working-directory: `frontend/`
- production: `true`

## Required GitHub Secrets

| Secret | Where to get it |
|--------|----------------|
| `RAILWAY_TOKEN` | railway.app → Account Settings → Tokens → New Token |
| `VERCEL_TOKEN` | vercel.com → Settings → Tokens → Create |
| `VERCEL_ORG_ID` | `team_MVTWK3ZoTBZ42xcXCTJTCex3` (already known) |
| `VERCEL_PROJECT_ID` | `prj_BkFZ2gtuinxki0nD0d1wGPKPNQBE` (already known) |

All secrets go to: github.com/Tr1Galki/clicker-app → Settings → Secrets and variables → Actions

## Flow

```
push to feature branch → test job → ✅/❌ on PR
push/merge to master   → test job → deploy job → Railway + Vercel updated
```

## Success Criteria

- `mvn test` passes in GitHub Actions on any push
- After merge to master: Railway redeploys backend, Vercel redeploys frontend
- PR shows green checkmark when tests pass
