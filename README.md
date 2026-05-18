# MVLChain

Interview-ready Android sample demonstrating **Jetpack Compose**, **MVI-style presentation** (state + pure-data intents + effects), **Clean Architecture** (`:app` / `:domain` / `:data`), **Dagger 2** (with an **activity subcomponent**), **Room** for local location/nickname cache, **Retrofit**, **Coroutines + Flow**, **Navigation Compose**, and **Google Maps**.

## Modules

| Module   | Responsibility |
|----------|----------------|
| `:app`   | Compose UI, ViewModels, navigation, theming, single-activity entry |
| `:domain`| Entities, repository contracts, use cases (pure Kotlin) |
| `:data`  | Retrofit APIs, DTOs + mappers, repository implementations, **Room** DB for map cache + slot nicknames, booking mock interceptor |

## Product flavors

`dev`, `qa`, and `prod` share the same codebase with different **book API base URLs**, **HTTP logging**, and **mock delay** for the booking endpoints (see `data/build.gradle.kts`). Application **name suffixes** are configured per flavor in `app/build.gradle.kts`.

## Secrets (do not commit)

Keys are merged at build time from **`local.properties`** and flavor files under **`config/`**:

1. `local.properties` (SDK path + optional shared keys)
2. **`config/<flavor>.properties`** for the active flavor (`dev`, `qa`, or `prod`) — **overrides** duplicate keys from `local.properties`.

Copy a template and edit (tracked examples only contain placeholders):

```bash
cp config/dev.properties.example config/dev.properties
```

```properties
MAPS_API_KEY=your_google_maps_key
AQI_API_TOKEN=your_waqi_token
GEO_API_KEY=optional_bigdatacloud_key
```

- **Google Maps (`MAPS_API_KEY`)**: injected into the app manifest via `manifestPlaceholders` in `:app` (`app/build.gradle.kts`).
- **WAQI (`AQI_API_TOKEN`)** and **geocoding (`GEO_API_KEY`)**: exposed as `BuildConfig` fields inside `:data` (`data/build.gradle.kts`).
- **`sdk.dir`** stays in `local.properties` only (Android Gradle Plugin expectation).

> Never commit real keys. For CI, generate `local.properties` and/or `config/<flavor>.properties` from your vault. You can keep filled `config/*.properties` out of git via your own `.gitignore` rules if your team prefers examples-only in the repo.

## Architecture notes

- **MVI (presentation)**: Each feature exposes **`uiState: StateFlow`**, **`processIntent(…)`** with **sealed intents** (no callback lambdas), and **`effects`** for one-shot navigation; Compose collects state and forwards user actions.
- **Dependency injection**: **Dagger** `@Singleton` **application graph** + **`MainActivitySubcomponent`** demonstrating subcomponents; ViewModels use **`MviViewModelFactory`** and `CompositionLocal` for Compose.
- **One-shot navigation / snackbars**: `Channel` / `SharedFlow` from ViewModels.
- **Repositories** hide Retrofit and the **OkHttp mock interceptor**; domain depends only on interfaces.
- **Booking mock**: `MockBooksInterceptor` simulates latency and JSON for `POST /v1/books` and `GET /v1/books` without changing use cases.
- **Location cache**: **Room** tables keyed by **`cache_${GeoCoordinate.roundedKey()}`** (three decimal places); slot nicknames in a separate table.
- **Map / booking flow**: map captures **A** and **B**, navigates to booking, then optional history restore with **refreshed AQI**.

## JDK 17

The build targets **Java 17**. If sync fails with *“Cannot find a Java installation… languageVersion=17”*, either:

- **Recommended:** keep the project as-is — `settings.gradle.kts` applies the **Foojay toolchain resolver**, so Gradle can **download JDK 17** on first sync.

- **Or** install a JDK 17 (e.g. Temurin) and point Android Studio to it: **Settings → Build, Execution, Deployment → Build Tools → Gradle → Gradle JDK**.

## Build

Open the project in **Android Studio Ladybug+** (AGP 8.7+, Kotlin 2.0+). Sync Gradle, select the **`devDebug`** variant, and run on a device or emulator with Play Services.

If Gradle wrapper scripts are missing, use Android Studio’s *Gradle* tool window or run `gradle wrapper` from a machine with Gradle installed.

## Tests

```bash
./gradlew testDebugUnitTest
```

Included examples: repository-style unit tests with **MockK**, **Turbine**, and coroutines test APIs.

## License

Demonstration project — use freely for learning and interviews.
