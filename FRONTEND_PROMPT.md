# Claude Code prompt — paste this into a fresh session opened on the frontend repo

Copy `API.md`, `WORKFLOWS.md`, and `postman_collection.json` from the backend repo
(`/home/shashika/aquaventure/Aquaventure-backend/`) into this frontend repo under `docs/api/`
before running the prompt below, so the fresh session can read them.

```
cp /home/shashika/aquaventure/Aquaventure-backend/{API.md,WORKFLOWS.md,postman_collection.json} docs/api/
```

---

## Prompt

I'm building the React frontend for AquaVenture, a platform connecting surf tourists ("surfers")
with surf schools in Eastern Sri Lanka. The backend is a Spring Boot API that just went through a
full rewrite — roles, auth, school approval, bookings, surf conditions, progress tracking — and
its contract is now final. Your job is to bring this frontend in line with it.

**Read these three files first, in full, before doing anything else:**
- `docs/api/API.md` — every endpoint grouped by role, request/response shapes, error codes, enums
- `docs/api/WORKFLOWS.md` — the actual call sequences for each user journey (surfer, school admin,
  platform admin, the booking lifecycle, notifications)
- `docs/api/postman_collection.json` — importable Postman collection with example payloads for
  every endpoint; use it to see exact field names when `API.md`'s prose isn't enough

**Then audit this repo** (don't change anything yet) and report back:
1. What auth/API-client setup already exists (base URL, token storage, interceptors).
2. What screens/components already exist and which backend features they assume.
3. Where the existing code's assumptions **conflict** with the new contract. Specifically check
   for: a base URL or paths missing the `/api` prefix (every backend path now starts with `/api`,
   including `/api/auth/**`); any naming mismatch like `Provider`/`Activity`/`Tourist` where the
   backend now uses `SurfSchool`/`Lesson`/`Surfer`; any role list that doesn't match
   `SURFER` / `SCHOOL_ADMIN` / `PLATFORM_ADMIN` (there is no separate `INSTRUCTOR` login role —
   instructors are a resource owned by a school, not an account).

**Then propose a plan** (entities/screens per role, which existing files to rework vs. replace,
state management for auth/token/role-based routing) and wait for my go-ahead before writing code.
Match whatever UI library/styling approach this repo already uses — don't introduce a new one.

**Implementation should follow the role split in `API.md`/`WORKFLOWS.md`:**
- **Public**: location browser, school search/filter/compare, school detail, surf conditions,
  open lesson slots, login/register (surfer and school).
- **Surfer** (after login): profile/preferences, recommendations, booking flow (slot → equipment →
  confirm), my bookings (cancel), progress log + summary, notifications.
- **School admin** (after login): a "pending approval" state that only allows viewing/editing the
  school profile until `schoolStatus` flips to `APPROVED` (check this on login and after re-auth);
  once approved: instructors/lessons/slots/equipment CRUD, incoming bookings (accept/reject/
  complete), dashboard, feedback on a surfer's progress entry, notifications.
- **Platform admin** (after login): school review queue (approve/reject with reason/suspend/
  reactivate), user management (search/suspend/reactivate), location CRUD, stats dashboard.

**Build and verify against the real running backend**, not just mocks:
```
DB_PASSWORD=1234 JWT_SECRET=aquaventure_super_secret_key_2026_change_this_in_prod_32bytes \
PLATFORM_ADMIN_EMAIL=admin@aquaventure.dev PLATFORM_ADMIN_PASSWORD='Admin@12345' \
SPRING_PROFILES_ACTIVE=dev \
./mvnw spring-boot:run
```
(run from `/home/shashika/aquaventure/Aquaventure-backend/`). The `dev` profile seeds 2 approved
schools with instructors/lessons/slots/equipment, 1 pending school, and a surfer with bookings —
use `seed.surfer@aquaventure.dev` / `Password1!`, `seed.arugam.owner@aquaventure.dev` /
`Password1!`, and `admin@aquaventure.dev` / `Admin@12345` as ready-made test accounts for the
three roles. Swagger UI is at `http://localhost:8080/swagger-ui.html` if you need to double-check
a response shape live.

Implement in phases matching the role sections above, keeping the app buildable and the golden
path (register → browse → book → school accepts → complete → progress entry) working after each
phase. After each phase, tell me what screens/files changed and what manual check in the browser
would confirm it works.
```
