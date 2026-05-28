# ExpiryX — Force app update API

Backend-driven minimum build enforcement for the ExpiryX Flutter app (android | ios | web).

## Endpoints

| Method | Path | Auth | Purpose |
|--------|------|------|---------|
| `GET` | `/api/app/config` | Public | Version config + `updateRequired` for splash |
| `GET` | `/api/health` | Public | Simple health check |

All other `/api/**` routes enforce version when the client sends `X-App-Build`.

## Client headers (protected APIs)

```
X-App-Platform: android   # android | ios | web
X-App-Version: 1.0.0      # display only
X-App-Build: 9              # primary compare field
```

If `X-App-Build` is omitted, the request is allowed (backward compatible until the app ships headers).

If `clientBuild < minBuild` for the platform → **HTTP 426** with JSON body below.

### Exempt from 426

- `GET /api/app/config`
- `POST /api/auth/send-otp`
- `GET /api/health`
- `/v3/api-docs`, `/swagger-ui/**`, `/files/**`

## Config API

### Request

```bash
curl "http://localhost:8080/api/app/config?platform=android&version=1.0.0&build=9"
```

| Query param | Required | Description |
|-------------|----------|-------------|
| `platform` | No | `android` (default), `ios`, or `web` |
| `version` | No | Client version name (display; not used for compare) |
| `build` | No | Client build number; used to compute `updateRequired` |

Response includes `Cache-Control: no-cache`.

### Response (example — update required)

After you set `min_build = 10` and `force_update = true` for android:

```json
{
  "platform": "android",
  "latestVersion": "1.0.0",
  "latestBuild": 10,
  "minVersion": "1.0.0",
  "minBuild": 10,
  "forceUpdate": true,
  "softUpdate": false,
  "updateRequired": true,
  "title": "Update required",
  "message": "Please update ExpiryX to continue.",
  "storeUrl": "https://play.google.com/store/apps/details?id=YOUR_PACKAGE",
  "releaseNotes": "Stability improvements"
}
```

### Server logic

- `updateRequired` = `build` param present AND `clientBuild < minBuild`
- `forceUpdate` = `updateRequired` AND DB `force_update`
- `softUpdate` = `clientBuild < latestBuild` AND NOT `forceUpdate` AND DB `soft_update`

## 426 response (protected API)

```bash
curl -H "Authorization: Bearer TOKEN" \
     -H "X-App-Platform: android" \
     -H "X-App-Version: 1.0.0" \
     -H "X-App-Build: 9" \
     http://localhost:8080/api/documents
```

```json
{
  "statusCode": 426,
  "message": "App update required. Please upgrade to the latest version.",
  "minBuild": 10,
  "latestBuild": 10,
  "forceUpdate": true,
  "storeUrl": "https://play.google.com/store/apps/details?id=YOUR_PACKAGE"
}
```

## Admin: change config without redeploy

Table: `app_version_config` (one row per platform).

```sql
-- Force everyone below build 10 to update (android)
UPDATE app_version_config
SET min_build = 10,
    latest_build = 10,
    min_version = '1.0.0',
    latest_version = '1.0.0',
    force_update = TRUE,
    soft_update = FALSE,
    title = 'Update required',
    message = 'Please update ExpiryX to continue.',
    store_url = 'https://play.google.com/store/apps/details?id=YOUR_PACKAGE',
    release_notes = 'Stability improvements',
    updated_at = NOW()
WHERE platform = 'android';
```

### Soft update only (optional prompt)

```sql
UPDATE app_version_config
SET min_build = 8,
    latest_build = 10,
    force_update = FALSE,
    soft_update = TRUE
WHERE platform = 'android';
```

Build 9 → `updateRequired: false`, `softUpdate: true` on config API.

## Release process (when to bump `min_build`)

1. Upload new build to Play Store / App Store (e.g. build **10**).
2. After the store approves and the binary is live, update DB:
   - `latest_build = 10`
   - `min_build = 10` (or keep lower for soft-only rollout)
   - `force_update = true` when you want to block old clients
3. All clients with `X-App-Build: 9` get `updateRequired: true` on splash and **426** on protected APIs.

**Do not** set `min_build` above the store build users can install.

## Default seed (V8 migration)

Initial rows use `min_build = 1` so development (build 9) is not blocked until you run the SQL above.

## Version comparison

| Rule | Behavior |
|------|----------|
| Primary | Integer `build` vs `min_build` |
| Secondary | `version` string is display only |
| `9 < 10` | Update required |
| `10 >= 10` | Allowed |

## Production base URL

```
https://doctracker-backend.onrender.com/api/app/config?platform=android&version=1.0.0&build=9
```

Replace store URLs and package id in `app_version_config` before forcing production users.
