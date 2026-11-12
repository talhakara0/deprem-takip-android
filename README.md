# Deprem Takip (Earthquake Tracker)

A portfolio Android app that displays recent earthquakes from the Turkish disaster agency
(AFAD) open data feed, works offline, and sends background notifications for new quakes in a
province selected by the user. Built with Kotlin, Jetpack Compose, Clean Architecture, Hilt,
Room, Retrofit, Coroutines/Flow, DataStore and WorkManager.

> **Türkçe özet:** AFAD açık deprem verisini gösteren, çevrimdışı çalışan, seçilen ilde eşik
> üzeri yeni depremler için WorkManager ile bildirim gönderen modern Android uygulaması.
> Compose + MVVM + Clean Architecture, Hilt, Room, Retrofit, DataStore, Coroutines/Flow ve 55
> JVM birim testi içerir. Bu depoda ekran görüntüsü yoktur; uygulama emülatör olmadan
> `./gradlew testDebugUnitTest assembleDebug` ile doğrulanır.

## Features

- Recent earthquakes list with **last 24 hours / last 7 days** range selection
- Color-coded magnitude badges and relative time labels ("12 dk önce")
- Filtering by **minimum magnitude**, province/district **text search**, sorting by time or
  magnitude
- Detail screen with all fields, coordinates and "Open in Maps" (`geo:` intent, no map SDK)
- Offline-first: Room cache, stale data is shown when the network is unavailable with an
  offline banner, plus pull-to-refresh
- Background alert check every 15 minutes with WorkManager: notifies for **new** quakes above
  a threshold in the selected province, Android 13+ `POST_NOTIFICATIONS` permission flow
- Settings screen (province, threshold, notifications on/off) backed by DataStore
- Material 3, light/dark theme, dynamic color on Android 12+, Turkish UI strings

## Architecture

```
┌─────────────────────────────────────────────────────────────┐
│  ui (Compose)                                               │
│  list · detail · settings · navigation · theme              │
│  ViewModels expose immutable UiState via StateFlow          │
└───────────────────────────┬─────────────────────────────────┘
                            │ use cases
┌───────────────────────────▼─────────────────────────────────┐
│  domain                                                     │
│  model (Earthquake, EarthquakeFilter, UserSettings, …)      │
│  usecase (GetEarthquakes, FilterEarthquakes,                │
│            CheckNewQuakesForAlert, GetEarthquakeDetail)     │
│  repository interfaces · Clock abstraction                  │
└───────────────────────────┬─────────────────────────────────┘
                            │ implementations (Hilt binds)
┌───────────────────────────▼─────────────────────────────────┐
│  data                                                       │
│  remote: Retrofit AfadApi + kotlinx.serialization DTOs      │
│  local: Room EarthquakeEntity/Dao + DataStore settings      │
│  repository: cache-first flow + network refresh             │
└───────────────────────────┬─────────────────────────────────┘
                            │
┌───────────────────────────▼─────────────────────────────────┐
│  worker: HiltWorker periodic alert check                    │
│  notification: channel + notifications                      │
└─────────────────────────────────────────────────────────────┘
```

## Tech stack

Kotlin 2.0, Jetpack Compose (BOM 2024.10), Material 3, Navigation Compose,
Lifecycle/ViewModel + `collectAsStateWithLifecycle`, Hilt + `HiltWorker`, Room, Retrofit +
kotlinx.serialization, OkHttp, DataStore Preferences, WorkManager, Coroutines/Flow.
Testing: JUnit4, MockWebServer, kotlinx-coroutines-test, Turbine, MockK (available).
Version catalog: `gradle/libs.versions.toml`. AGP 8.7.3, Gradle 8.11.1, JDK 17,
compileSdk/targetSdk 35, minSdk 26.

## Design decisions

- **Offline-first**: the UI always renders from the Room cache. A network refresh writes into
  the cache and updates the flow; failures never delete cached data, they only raise the
  offline flag. This keeps the list usable on a flaky mobile connection.
- **Tolerant parsing**: every DTO field is nullable with a default, unknown JSON fields are
  ignored, and missing/invalid dates degrade to `null` instead of failing the whole response.
  Missing event IDs get a deterministic fallback key so Room can still store the row.
- **WorkManager constraints**: the periodic worker requires `NetworkType.CONNECTED`, runs at
  the platform minimum interval (15 minutes) and uses exponential backoff. The system may
  delay execution (Doze/battery optimizations), so alerts are best-effort, not real-time.
- **No map SDK**: the detail screen only launches a `geo:` intent. This avoids API keys,
  Play Services dependencies and binary size, while still letting the device's map app open
  the location.
- **Testable time**: a `Clock` interface is injected everywhere time matters (range cutoff,
  relative labels, alert window), so tests use a fixed instant.
- **Turkish-aware search**: `TurkishText` folds `İ/I/ı`, `Ş/Ğ/Ü/Ö/Ç` so "izmir" matches
  "İzmir" and "IGDIR" matches "Iğdır".

## Build and test

```bash
# local.properties must contain:
# sdk.dir=/opt/homebrew/share/android-commandlinetools

./gradlew testDebugUnitTest   # 55 JVM unit tests
./gradlew assembleDebug       # debug APK
./gradlew lintDebug           # lint (warnings allowed, build passes)
```

APK output: `app/build/outputs/apk/debug/app-debug.apk`.
`local.properties`, `build/` and `.gradle/` are git-ignored.

## Tests

There are **55 unit tests** (all JVM, no emulator, no real network):

- DTO → domain mapping, missing fields, fallback IDs, date parsing
- Retrofit parsing against MockWebServer (unknown fields, missing values, query parameters)
- Filtering/sorting use cases, magnitude threshold, Turkish case-insensitive province search
- Alert use case: new vs. seen IDs, threshold boundary, province matching, 24-hour window,
  disabled notifications, missing magnitude/date
- Repository: refresh success/failure with fake API and fake DAO, offline cache preservation,
  8-day pruning, cache observation
- ViewModels: loading → success, refresh error with cached data + offline flag, filter/range
  changes, detail lookup, settings updates

## Data source and attribution

Earthquake data: **AFAD Açık Kaynak Deprem Verisi**
(`https://deprem.afad.gov.tr/apiv2/event/filter`, which currently redirects to
`servisnet.afad.gov.tr/apigateway/deprem/...`; OkHttp follows it). No API key is required.
AFAD sends numeric fields as quoted strings (`"magnitude":"2.7"`); the lenient JSON
config handles this and `AfadApiTest` pins it with a captured real payload. The app is not
affiliated with AFAD; data is used for demonstration purposes only and may be delayed or
incomplete. Please cite AFAD when reusing the data.

## Limitations

- No screenshots are included in this repository (no emulator available in the build
  environment); verification is limited to compilation, lint and unit tests.
- Background intervals are subject to WorkManager/OS scheduling and battery optimizations.
- Only the last 200 events are fetched per request; cache rows older than 8 days are pruned.
- The AFAD API is undocumented; field names are parsed tolerantly and may change.
- No instrumentation/UI tests; Compose screens are covered indirectly through ViewModel
  tests.
- Single `app` module with layered packages (intentionally, for a compact portfolio project).
