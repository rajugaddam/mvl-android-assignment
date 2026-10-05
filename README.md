# MVL Android Assignment

An Android app where you pick two points (A and B) on a map, see the air quality at each, give them
nicknames, book a trip between them and view this month's booking history.

Built with Jetpack Compose (single activity), MVVM, Hilt, Coroutines/Flow, Retrofit, Room and the
Google Maps SDK.

## Screens

1. **Map** – a fixed pin in the center and the air quality for that spot in the top-right corner.
   The main button goes Set A, Set B, Book.
2. **Place detail** – the address and air quality of A or B, and an optional nickname (max 20 characters).
3. **Booking result** – A and B with the price returned by the server. Going back clears the map.
4. **History** – this month's bookings with total count and total price.
5. **Saved places** (optional) – tap an empty A or B box to reuse a place you've looked up before.

All the optional items are done: local cache of looked-up places, Screen 5, tapping a history item to
rebook it (air quality is fetched again), and mock responses generated from the request.

## Running the project

Requirements: Android Studio 2025.2.1 or newer, JDK 17, Android SDK 36.

### API keys

Keys are not committed. Add them to `local.properties` in the project root (this file is git-ignored):

```properties
MAPS_API_KEY=your_google_maps_key
WAQI_TOKEN=your_waqi_token
# Only needed for the prod flavor
# BOOKS_BASE_URL=https://your.server/
```

- Maps key: Google Cloud Console, enable "Maps SDK for Android", then create an API key.
  If you restrict it to Android apps, add both `com.mvl.assignment.mock` and `com.mvl.assignment`.
- WAQI token: free at https://aqicn.org/data-platform/token/
- Reverse geocoding uses BigDataCloud's free endpoint, which doesn't need a key.

Rebuild after changing a key. The project still builds without keys; the map then shows a
"key missing" message instead of crashing.

### Build variants

- `mockDebug` (default) – the books API is served by a mock backend inside the app.
- `prodDebug` – calls a real server at `BOOKS_BASE_URL`.

```bash
./gradlew installMockDebug
./gradlew testMockDebugUnitTest
```

## Architecture

The code is split into three layers in one module:

- **ui** – Compose screens, each with its own ViewModel. ViewModels expose a `StateFlow` of UI state
  and only call use cases.
- **domain** – plain Kotlin: models, repository interfaces and use cases. Business rules live here,
  e.g. address formatting, the Set A / Set B / Book step logic, nickname rules and the cache key.
- **data** – repository implementations, Retrofit APIs, Room cache and DTO mappers.

Hilt provides everything (network, repositories, database, location, dispatchers). The A/B selection
shared between screens is kept in a singleton repository; navigation only passes ids.

### APIs

| Purpose | Request |
|---|---|
| Air quality | `GET https://api.waqi.info/feed/geo:{lat};{lng}/` |
| Address | `GET https://api.bigdatacloud.net/data/reverse-geocode-client` |
| Book a trip | `POST /books` with `{ "a": place, "b": place }` |
| History | `GET /books?year={year}&month={month}` |

A `place` has `latitude`, `longitude`, `name`, `aqi` and an optional `nickname`. The response also
includes an `id`, which is used to open the result screen and to key the history list. Some locations
have no air quality data; those show `-`.

### Mock backend

In the mock variant, an OkHttp interceptor (`MockBooksInterceptor`) answers `/books` requests, so the
app goes through Retrofit exactly as it would with a real server. Only the Hilt module in `src/mock`
differs from `src/prod`; repositories, use cases and UI are the same.

- `POST /books` validates the body, prices the trip by distance (3,800 + 1,000 per km) and saves it.
  Bookings are stored in the mock's own JSON file, so they're still there after restarting the app.
- `GET /books` returns that month's bookings plus some sample trips between real Seoul locations.
  The samples come from `MockSeedConfig` and are the same every time for a given month.
- Invalid requests get 400, unknown paths 404 and wrong methods 405.

### Async and state

- Moving the map triggers an air quality request after a 300 ms debounce. A newer position cancels the
  previous request, so an old result can't overwrite a new one.
- The address and air quality for a place are fetched in parallel.
- Buttons ignore repeated taps while a request is running.
- Network work runs on an injected IO dispatcher. Every call returns a `Result`. Screens show loading
  and error states with retry, and the lists show a message when they're empty.

## Tests

```bash
./gradlew testMockDebugUnitTest testProdDebugUnitTest
```

Unit tests cover the domain rules and use cases, the API contracts (method, path, query and body,
using MockWebServer), the mock backend, and the Map and History ViewModels. There are no UI or
instrumented tests.

## Notes

- When a place is already in the cache, its stored air quality is reused. The live value on the map and
  rebooking from History always fetch fresh data.
- Confirming on the result screen also clears A and B, since the booking is finished.
- Keys end up in the APK like any client-side key. For production the Maps key should be restricted to
  the app's signing certificate, and the WAQI token should go through a backend. The HTTP log is only
  enabled in debug builds and never includes the token.
