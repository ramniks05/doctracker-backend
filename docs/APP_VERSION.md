# ExpiryX — Force app update API

Version rules are configured with **environment variables** (or `application.yml`). No database or manual SQL on the server.

## Endpoints

| Method | Path | Auth |
|--------|------|------|
| `GET` | `/api/app/config` | Public |
| `GET` | `/api/health` | Public |

Protected APIs return **426** when `X-App-Build` &lt; `min-build` for that platform.

## Client headers

```
X-App-Platform: android
X-App-Version: 1.0.0
X-App-Build: 9
```

## Force update after Play Store release (Render)

1. Upload build **10** to Play Store; wait until it is live.
2. In **Render → Environment**, set for Android:

| Variable | Example | Meaning |
|----------|---------|---------|
| `APP_ANDROID_MIN_BUILD` | `10` | Block builds below 10 |
| `APP_ANDROID_LATEST_BUILD` | `10` | Latest published build |
| `APP_ANDROID_FORCE_UPDATE` | `true` | Blocking update on splash |
| `APP_ANDROID_STORE_URL` | `https://play.google.com/store/apps/details?id=YOUR_PACKAGE` | Play Store link |
| `APP_ANDROID_UPDATE_TITLE` | `Update required` | Dialog title |
| `APP_ANDROID_UPDATE_MESSAGE` | `Please update ExpiryX to continue.` | User message |

3. Save — Render redeploys automatically. No SQL.

### Soft update only (optional prompt)

```
APP_ANDROID_MIN_BUILD=8
APP_ANDROID_LATEST_BUILD=10
APP_ANDROID_FORCE_UPDATE=false
APP_ANDROID_SOFT_UPDATE=true
```

Build 9 → `updateRequired: false`, `softUpdate: true`.

### iOS / Web

Use the `APP_IOS_*` and `APP_WEB_*` variables (same pattern as `APP_ANDROID_*`). Defaults are in `application.yml`.

## Local / `.env` example

```properties
APP_ANDROID_MIN_BUILD=10
APP_ANDROID_LATEST_BUILD=10
APP_ANDROID_FORCE_UPDATE=true
APP_ANDROID_STORE_URL=https://play.google.com/store/apps/details?id=YOUR_PACKAGE
```

## Config API

```bash
curl "https://doctracker-backend.onrender.com/api/app/config?platform=android&version=1.0.0&build=9"
```

## 426 example

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

## All environment variables

| Android | iOS | Web |
|---------|-----|-----|
| `APP_ANDROID_MIN_BUILD` | `APP_IOS_MIN_BUILD` | `APP_WEB_MIN_BUILD` |
| `APP_ANDROID_LATEST_BUILD` | `APP_IOS_LATEST_BUILD` | `APP_WEB_LATEST_BUILD` |
| `APP_ANDROID_MIN_VERSION` | `APP_IOS_MIN_VERSION` | `APP_WEB_MIN_VERSION` |
| `APP_ANDROID_LATEST_VERSION` | `APP_IOS_LATEST_VERSION` | `APP_WEB_LATEST_VERSION` |
| `APP_ANDROID_FORCE_UPDATE` | `APP_IOS_FORCE_UPDATE` | `APP_WEB_FORCE_UPDATE` |
| `APP_ANDROID_SOFT_UPDATE` | `APP_IOS_SOFT_UPDATE` | `APP_WEB_SOFT_UPDATE` |
| `APP_ANDROID_UPDATE_TITLE` | `APP_IOS_UPDATE_TITLE` | `APP_WEB_UPDATE_TITLE` |
| `APP_ANDROID_UPDATE_MESSAGE` | `APP_IOS_UPDATE_MESSAGE` | `APP_WEB_UPDATE_MESSAGE` |
| `APP_ANDROID_STORE_URL` | `APP_IOS_STORE_URL` | `APP_WEB_STORE_URL` |
| `APP_ANDROID_RELEASE_NOTES` | `APP_IOS_RELEASE_NOTES` | `APP_WEB_RELEASE_NOTES` |

Defaults (no env set): `min-build=1`, `latest-build=9`, `force-update=false` — current app build 9 keeps working.

## Version comparison

Compare **integer build** only (`9` vs `10`). Version name strings are for display.
