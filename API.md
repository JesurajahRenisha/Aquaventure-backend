# AquaVenture API

All endpoints are prefixed with `/api`. Every response is wrapped as:

```json
{ "success": true, "data": { ... }, "error": null, "timestamp": "2026-10-21T09:00:00Z" }
```

On error:

```json
{ "success": false, "data": null, "error": { "code": "VALIDATION_ERROR", "message": "...", "fieldErrors": { "field": "message" } }, "timestamp": "..." }
```

Authenticate with `Authorization: Bearer <token>` (obtained from `/api/auth/login`). Paginated list endpoints accept standard Spring `page`, `size`, `sort` query params and return:

```json
{ "content": [...], "page": 0, "size": 20, "totalElements": 42, "totalPages": 3 }
```

Error codes you'll see across endpoints: `VALIDATION_ERROR` (400), `UNAUTHORIZED` (401), `FORBIDDEN` / `FORBIDDEN_OWNERSHIP` (403), `SCHOOL_NOT_APPROVED` (403), `NOT_FOUND` (404), `DUPLICATE_RESOURCE` / `SLOT_OVERLAP` / `SLOT_FULL` / `INVALID_STATE_TRANSITION` / `CONCURRENT_MODIFICATION` (409), `EXTERNAL_SERVICE_UNAVAILABLE` (503).

---

## Public (no token required)

| Method | Path | Notes |
|---|---|---|
| POST | `/auth/register/surfer` | Body: `firstname, lastname, email, password, phone?, nationality?, local?`. Active immediately. |
| POST | `/auth/register/school` | `multipart/form-data`: `ownerFirstname, ownerLastname, email, password, schoolName, description?, address?, contactPhone?, contactEmail?, businessRegistrationNumber, locationIds[]?` + `documents` file parts. Creates a `SCHOOL_ADMIN` user and a `PENDING` school. |
| POST | `/auth/login` | Body: `email, password`. Response includes `role`, and for school admins, `schoolId` + `schoolStatus`. |
| GET | `/locations` | Active surf locations. |
| GET | `/locations/{id}/conditions` | Current + next-3-day forecast (wave height, swell, wind, temp) with a `SUITABLE`/`NOT_SUITABLE` label per skill level. 503 if the upstream provider is down. |
| GET | `/schools?locationId=&skillLevel=&lessonType=&minPrice=&maxPrice=&date=&sort=name\|price` | Only `APPROVED` schools, paginated. |
| GET | `/schools/{id}` | Public school detail: profile + instructors + lessons + equipment. Never includes registration number, documents, or rejection reason. |
| GET | `/schools/compare?ids=1,2,3` | 2–5 school ids. Side-by-side price range, lesson types, instructor count, languages, skill levels, equipment availability. |
| GET | `/lessons/{id}/slots?from=&to=` | Only `OPEN` slots in the given time range. |

## Surfer (`SURFER` role)

| Method | Path | Notes |
|---|---|---|
| GET / PUT | `/surfer/me/profile` | Skill level, preferred location/lesson type, budget, board type, bio. Lazily created on first `GET`. |
| GET | `/surfer/me/recommendations` | Lessons matching skill level + preferred location (falls back to skill-only if no location match). |
| POST | `/bookings` | Body: `lessonSlotId, participants, equipmentItems?[{equipmentId, quantity, rentalUnit}], note?`. Server computes total price; status starts `PENDING`. |
| GET | `/surfer/me/bookings?status=` | Paginated, own bookings only. |
| PATCH | `/surfer/me/bookings/{id}/cancel` | Only while `PENDING`/`ACCEPTED` and before the slot starts; releases reserved capacity. |
| GET | `/surfer/me/progress?...` | Paginated progress log, own entries only. |
| POST | `/surfer/me/progress` | Body: `date, locationId?, skillFocus?, durationMinutes?, selfRating? (1-5), notes?`. |
| PUT | `/surfer/me/progress/{id}` | Fills in a draft (or edits an existing) entry. |
| DELETE | `/surfer/me/progress/{id}` | |
| GET | `/surfer/me/progress/summary` | Total lessons, hours in the water, sessions per location, rating trend, current skill level. |

> A `PENDING` draft progress entry is created automatically whenever a school marks a booking `COMPLETED`.

## Surf School Admin (`SCHOOL_ADMIN` role)

All sub-resource endpoints below require the school's status to be `APPROVED` (except the profile endpoints, which work while `PENDING` too, and are blocked only while `SUSPENDED`/`REJECTED`). The school is always resolved from the authenticated user — never from a client-supplied id — so one school can never read or modify another's data.

| Method | Path | Notes |
|---|---|---|
| GET / PUT | `/school/me` | Profile: name, description, address, contact, logo, images, served locations. |
| GET | `/school/me/documents/{index}` *(under `/schools/{schoolId}/documents/{index}`)* | Streams a verification document; owner or `PLATFORM_ADMIN` only. |
| GET / POST | `/school/me/instructors` | List (paginated) / create. |
| PUT / DELETE | `/school/me/instructors/{id}` | |
| GET / POST | `/school/me/lessons` | |
| PUT / DELETE | `/school/me/lessons/{id}` | |
| GET / POST | `/school/me/slots` | `POST`/`PUT` reject overlapping slots for the same instructor (409 `SLOT_OVERLAP`). |
| PUT / DELETE | `/school/me/slots/{id}` | `DELETE` blocked if the slot already has bookings. |
| GET / POST | `/school/me/equipment` | |
| PUT / DELETE | `/school/me/equipment/{id}` | |
| GET | `/school/me/bookings?status=` | Paginated. |
| PATCH | `/school/me/bookings/{id}/accept` | Re-checks capacity with optimistic-lock retry; 409 `SLOT_FULL` or `CONCURRENT_MODIFICATION` on contention. |
| PATCH | `/school/me/bookings/{id}/reject` | Body: `{ "note": "..." }`. |
| PATCH | `/school/me/bookings/{id}/complete` | Triggers the surfer's draft progress entry. |
| PATCH | `/school/me/progress/{id}/feedback` | Body: `{ "feedback": "..." }`. Only on entries linked to one of the school's own `COMPLETED` bookings. |
| GET | `/school/me/dashboard` | Pending booking count, next 10 upcoming open slots, total + this-month revenue, completed booking count. |

## Platform Admin (`PLATFORM_ADMIN` role)

| Method | Path | Notes |
|---|---|---|
| GET | `/admin/schools?status=` | Paginated. |
| GET | `/admin/schools/{id}` | Full detail incl. documents, registration number, rejection reason. |
| PATCH | `/admin/schools/{id}/approve` | Only from `PENDING`. |
| PATCH | `/admin/schools/{id}/reject` | Body: `{ "reason": "..." }` (required). Only from `PENDING`. |
| PATCH | `/admin/schools/{id}/suspend` | Only from `APPROVED`. |
| PATCH | `/admin/schools/{id}/reactivate` | Only from `SUSPENDED`. |
| GET | `/admin/users?role=&status=&q=` | Paginated, searchable. |
| PATCH | `/admin/users/{id}/suspend` / `/reactivate` | |
| GET / POST | `/admin/locations` | Full location list incl. inactive / create. |
| PUT / DELETE | `/admin/locations/{id}` | `DELETE` soft-deletes (`active=false`). |
| GET | `/admin/stats` | Users by role, schools by status, bookings by status, bookings by location. |

## Any authenticated user

| Method | Path | Notes |
|---|---|---|
| GET | `/notifications/me?...` | Paginated, own notifications only. |
| PATCH | `/notifications/{id}/read` | |
| PATCH | `/notifications/me/read-all` | |

---

## Domain enums

- **Role**: `SURFER`, `SCHOOL_ADMIN`, `PLATFORM_ADMIN`
- **UserStatus**: `ACTIVE`, `SUSPENDED`
- **SchoolStatus**: `PENDING`, `APPROVED`, `REJECTED`, `SUSPENDED`
- **SkillLevel**: `BEGINNER`, `INTERMEDIATE`, `ADVANCED`
- **LessonType**: `GROUP`, `PRIVATE`, `SEMI_PRIVATE`
- **Currency**: `LKR`, `USD`
- **LessonSlotStatus**: `OPEN`, `FULL`, `CANCELLED`
- **EquipmentType**: `SURFBOARD`, `SOFTBOARD`, `WETSUIT`, `LEASH`, `RASH_GUARD`
- **BookingStatus**: `PENDING`, `ACCEPTED`, `REJECTED`, `CANCELLED`, `COMPLETED` (transitions enforced in `BookingService` — see allowed-transition map)
- **RentalUnit**: `HOUR`, `DAY`
- **NotificationType**: `SCHOOL_APPROVED`, `SCHOOL_REJECTED`, `SCHOOL_SUSPENDED`, `SCHOOL_REACTIVATED`, `BOOKING_CREATED`, `BOOKING_ACCEPTED`, `BOOKING_REJECTED`, `BOOKING_CANCELLED`, `BOOKING_COMPLETED`

## Running locally

```
DB_PASSWORD=... JWT_SECRET=... PLATFORM_ADMIN_EMAIL=admin@aquaventure.dev PLATFORM_ADMIN_PASSWORD=... ./mvnw spring-boot:run
```

Add `-Dspring-boot.run.profiles=dev` (or `SPRING_PROFILES_ACTIVE=dev`) to also seed 2 approved schools + 1 pending, instructors/lessons/slots for the next 14 days, equipment, and a surfer with a completed + a pending booking.

Swagger UI: `http://localhost:8080/swagger-ui.html`.

## Known frontend-impacting gap

This backend uses the task's own domain naming (`SurfSchool`, `Lesson`, `Surfer`, roles `SURFER`/`SCHOOL_ADMIN`/`PLATFORM_ADMIN`) and prefixes every path with `/api`, including `/api/auth/**`. The existing React frontend (a separate repo) was built against a different contract (`Provider`/`Activity`/`Tourist` naming, an `INSTRUCTOR` login role, and un-prefixed `/auth/**` paths) and will need updating to match this API — that work was out of scope for this backend session.
