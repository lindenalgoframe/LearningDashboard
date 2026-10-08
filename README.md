# Learning Dashboard (Android · Kotlin · Jetpack Compose)

**Run:** open in Android Studio (Ladybug+, JDK 17) → run `app`. **APK:** `./gradlew assembleRelease` → `app/build/outputs/apk/release/`. **Tests:** `./gradlew testDebugUnitTest`.
**Demo login:** `demo@learning.com` / `password123` (format is validated on-device; credentials by the mock API). **Offline demo:** load courses → airplane mode → kill & reopen the app.

**Stack:** MVVM · Hilt · Coroutines/Flow · Retrofit + OkHttp + kotlinx.serialization · Room · DataStore · Navigation Compose (type-safe routes).
The "backend" is an OkHttp interceptor (`MockApiInterceptor`) serving `assets/mock/*.json`, so the real network stack runs end-to-end, and it throws an `IOException` when the device is offline, so the offline path is genuine, not simulated.

### 1. Architecture
`Compose UI → ViewModel (StateFlow<UiState>) → Repository (domain interface) → Retrofit API + Room`.
Each screen's state is a sealed type (`Loading / Empty / Error / Success`), so impossible states (e.g. "loading" and "error" together) can't be represented. The ViewModel derives it by `combine`-ing the cached data stream with the latest refresh status. The domain layer (models, repository interfaces, `ProgressCalculator`, `LoginValidator`) is plain Kotlin, so ViewModels are tested with fakes. I skipped one-line use-case classes deliberately; I'd add them when logic is shared between ViewModels. Navigation reacts to session state, so login, logout and a future token expiry all go through one code path.

### 2. Offline support
Offline-first, with **Room as the single source of truth**: the UI only ever observes Room, and the network only ever writes into Room (in transactions). If a refresh fails, the cached courses stay on screen with an "offline" banner. The error screen appears only when the cache is empty. Course progress is computed in SQL plus `ProgressCalculator` from cached lessons, so marking a lesson complete updates both screens instantly, offline too. Local completions are flagged `pending_sync`, and `LessonMerger` keeps them through refreshes so a pull-to-refresh never undoes the user's work. The session token is persisted, so the app opens straight to cached data offline.

### 3. Security
Store the access token encrypted: an AES-GCM key generated in the **Android Keystore** (hardware-backed where available) encrypts the token before it goes into DataStore, never plain SharedPreferences. Use a short-lived access token plus a refresh token, refreshed through an OkHttp `Authenticator`. I'd also add: HTTPS with certificate pinning, `allowBackup=false` (already set), clearing the cache on logout (already done), R8 obfuscation, and Play Integrity for sensitive calls.

### 4. Scale (1M users, hundreds of courses)
1. **Paging 3 + `RemoteMediator`** with server-side cursor pagination, instead of loading the full list.
2. **Sync engine:** a WorkManager job (network constraint, backoff) that pushes `pending_sync` rows idempotently, plus delta sync using `updatedSince` or ETags.
3. **HTTP caching and CDN:** `Cache-Control`/ETag on course catalogues, and images through Coil served from a CDN.
4. **Modularisation** (`:core:data`, `:core:database`, `:feature:*`) for build speed and team ownership, plus baseline profiles for startup.
5. **Observability:** Crashlytics, performance traces, API error-rate dashboards, feature flags, and staged rollouts.

### 5. Second platform (iOS)
The same layering in Swift: **SwiftUI views → `@Observable` ViewModels → repository protocol → `URLSession` async/await + SwiftData** (or Core Data). UI state is an `enum` with associated values, data flows through `AsyncStream` or Observation, the token goes in the **Keychain**, and connectivity comes from `NWPathMonitor`. Tests use XCTest with protocol-based fakes. Kotlin Multiplatform could also share the domain and data layers, with only SwiftUI written natively.
