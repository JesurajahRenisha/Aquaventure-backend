# AquaVenture Backend

Spring Boot 3 (Java 17) + PostgreSQL backend for **AquaVenture**, the Smart Surf
Tourism Management System. Four roles: **Tourist**, **Provider** (surf school),
**Instructor**, **Admin**.

## Stack

- Spring Boot 3.5, Spring Web, Spring Data JPA, Spring Security, Spring WebFlux
  (`WebClient` only, for the weather integration)
- PostgreSQL (Flyway-managed schema, `ddl-auto=validate`)
- JWT auth (`jjwt`), BCrypt password hashing
- Bean Validation on all request DTOs
- springdoc-openapi (Swagger UI)
- Lombok

## Prerequisites

- JDK 17
- A local PostgreSQL instance (native install or Docker)

## 1. Start Postgres

```bash
docker compose up -d
```

Starts Postgres 16 on `localhost:5432`, database `aquaventure_db`, user
`postgres`, password `Postgres@123`. **If you already have a native Postgres
service on port 5432**, this will fail to bind that port — either stop the
native service first, or just point the env vars below at your existing
instance instead.

## 2. Configure environment

The `local` Spring profile (active by default) has working defaults matching
a native Postgres install using password `Postgres@123`. To override:

| Env var       | Default (local profile)                            |
|---------------|------------------------------------------------------|
| `DB_URL`      | `jdbc:postgresql://localhost:5432/aquaventure_db`     |
| `DB_USERNAME` | `postgres`                                            |
| `DB_PASSWORD` | `Postgres@123`                                        |
| `JWT_SECRET`  | a local-only default (**must** be overridden outside local) |
| `WEATHER_API_KEY` | unused by default (see Weather section below)    |

The `dev` profile has no defaults — everything above must be set explicitly
when running with `SPRING_PROFILES_ACTIVE=dev`.

## 3. Run the app

```bash
./mvnw spring-boot:run
```

Starts on `http://localhost:8080`. Swagger UI: `http://localhost:8080/swagger-ui.html`.
OpenAPI JSON: `http://localhost:8080/v3/api-docs`.

## Database migrations

This backend went through two schema generations, tracked as four Flyway
migrations under `src/main/resources/db/migration`:

- **V1 / V2** — the original schema for an earlier "surf lessons" version of
  this project, plus its seed data. Kept byte-for-byte as originally written
  (Flyway checksums the applied migrations against the files on disk — these
  cannot be edited without breaking every environment that already ran them).
- **V3** — restructures that schema into the current tourism-system schema:
  restructures `users` (splits into `name`, adds `phone_number`), creates
  `tourist` / `provider` / `admin` / `instructor` / `equipment` /
  `surf_location` / `surf_activity` / `booking` / `surf_progress` /
  `recommendation` / `weather_information`, migrates every real user account
  into the correct new role-profile table (preserving provider business
  names), and drops the now-superseded tables (`lessons`, `bookings`,
  `reviews`, etc.) along with their demo data.
- **V4** — seeds sample data for the entities introduced in V3 (locations,
  activities, an instructor, equipment, a booking, progress, a recommendation,
  a weather reading), looked up by email rather than hardcoded ids since real
  user accounts already occupy the low ids.

On a brand-new empty database, Flyway just runs all four in sequence and ends
up in the same final state. `spring.flyway.baseline-on-migrate` is enabled so
Flyway can also adopt a database it's never tracked before.

### Seed accounts

All seeded accounts use the password **`Passw0rd!`**:

| Role       | Email                              | Notes                                    |
|------------|-------------------------------------|-------------------------------------------|
| Admin      | `admin@aquaventure.com`            |                                             |
| Provider   | `ravi@aquaventure.com`             | Business: Ravi's Wave School, has activities + equipment |
| Provider   | `priya@aquaventure.com`            | Business: Bay Break Surf Co.               |
| Instructor | `nadia.instructor@aquaventure.com` | Employed by Ravi's Wave School             |
| Tourist    | `kasun@example.com`                | BEGINNER, has a confirmed+paid booking, logged progress, a recommendation |
| Tourist    | `amaya@example.com`                | INTERMEDIATE, has a pending booking        |

Plus every real account that existed before this schema migration (preserved,
role unchanged, `role=SURFER` internally -- see note below).

## Sample requests

**Register**
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"name":"Jane Doe","email":"jane@example.com","password":"Passw0rd!","role":"TOURIST"}'
```

**Login** (returns `{token, userId, name, email, role, profileId}`)
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"kasun@example.com","password":"Passw0rd!"}'
```

**Browse activities (public)**
```bash
curl http://localhost:8080/api/activities
```

**Book an activity (authenticated tourist)**
```bash
curl -X POST http://localhost:8080/api/bookings \
  -H "Authorization: Bearer <token>" -H "Content-Type: application/json" \
  -d '{"activityId":1,"bookingDate":"2027-01-01T09:00:00"}'
```

**Provider confirms, tourist pays**
```bash
curl -X PUT http://localhost:8080/api/bookings/<id>/status \
  -H "Authorization: Bearer <provider-token>" -H "Content-Type: application/json" \
  -d '{"status":"CONFIRMED"}'

curl -X POST http://localhost:8080/api/bookings/<id>/payment \
  -H "Authorization: Bearer <tourist-token>" -H "Content-Type: application/json" \
  -d '{"paymentMethod":"card"}'
```

## Weather integration

`GET /api/weather/location/{locationId}` calls the free, keyless Open-Meteo
marine + forecast APIs (no `WEATHER_API_KEY` needed) for a fixed reference
point (Arugam Bay, Sri Lanka — `SurfLocation` has no lat/long columns per the
design, so every location currently resolves to the same coordinates).
Readings are cached in `weather_information` and reused for
`weather.cache-ttl-minutes` (default 60). If the external call fails for any
reason (offline grading environment, API downtime), it falls back to a
plausible simulated reading so the feature still works end to end.

## Role note: TOURIST vs SURFER

The design ERD's role enum is `TOURIST / PROVIDER / INSTRUCTOR / ADMIN`. This
backend's `Role` enum uses the constant `SURFER` instead of `TOURIST` for the
customer role, purely so the real accounts already registered under the
previous "surf lessons" version of this project didn't need to be renamed.
Both `"TOURIST"` and `"SURFER"` are accepted (case-insensitively) anywhere a
role string is submitted (see `Role.fromJson`); responses always report
`"SURFER"`.

## Business rules implemented

- **Booking flow** matches the sequence diagram: create (`PENDING`/`UNPAID`) →
  provider confirms/cancels → tourist pays (mock/stub gateway, no real
  processor) → `CONFIRMED`/`PAID`. Cancelling a paid booking marks it
  `REFUNDED`.
- **Recommendations**: `POST /api/recommendations/generate/{touristId}`
  matches the tourist's skill level against each location's difficulty level,
  minimum safety rating (3/5), and current wave height for that skill level,
  and persists one `Recommendation` row per suitable location.
- **Password reset**: `POST /api/auth/reset-password` is a simplified
  email+new-password flow — there's no SMTP/emailed-token integration wired
  up.

## Project structure

```
src/main/java/com/aquaventure/
  config/      SecurityConfig, OpenApiConfig
  security/    JwtUtil, JwtAuthFilter, UserDetailsServiceImpl, UserPrincipal
  controller/  REST controllers (one per resource/role area)
  dto/         Request/response DTOs (entities are never exposed directly)
  entity/      JPA entities + enums
  repository/  Spring Data JPA repositories
  service/     Business logic (transactional boundary lives here)
    service/weather/  WeatherClient (WebClient) + WeatherReading
  mapper/      Entity -> DTO mapping
  exception/   Custom exceptions + GlobalExceptionHandler (consistent
               {timestamp, status, error, message, path} JSON error shape)
src/main/resources/
  application.yml, application-local.yml, application-dev.yml
  db/migration/  Flyway migrations (see "Database migrations" above)
src/test/java/com/aquaventure/
  service/       Unit tests (Mockito) for booking + recommendation logic
  integration/   Full-stack tests (MockMvc + real Postgres) for auth,
                 the booking flow, and role-based access control
```

## Running tests

Tests need a reachable Postgres and run against a **separate** database
(`aquaventure_test_db` by default, created empty — Flyway builds the full
schema from scratch, exercising the whole V1-V4 chain) so they never touch
your dev data:

```bash
./mvnw test -Dspring.profiles.active=test
```

## Notes / known follow-ups

- The React frontend (`aquaventure-frontend/`) predates this rebuild and does
  not yet call these APIs — it still uses its own mock data and a different
  role/entity model (Tourist/Provider/Admin dashboards, no Instructor
  dashboard). Per the project plan, backend work was completed and verified
  first; frontend wiring is a separate follow-up.
- `SurfLocation` has no coordinates, so weather is not truly per-location yet
  (see "Weather integration" above).
- No real payment gateway; `Booking.status`/`paymentStatus` are tracked
  fields only, updated by a stub "payment" endpoint.
