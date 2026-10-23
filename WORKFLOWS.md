# AquaVenture — Frontend Workflows

Step-by-step sequences of API calls for each user journey, matching the requests in `postman_collection.json`. Use alongside `API.md` (full endpoint reference). All paths are relative to `/api`.

---

## 1. Surfer journey

```
1.  POST /auth/register/surfer                       → create account (active immediately)
2.  POST /auth/login                                  → get { token, role: "SURFER" }
3.  GET  /locations                                   → populate a location picker
4.  GET  /schools?locationId=&skillLevel=&...         → school directory / search results page
5.  GET  /schools/{id}                                → school detail page (instructors, lessons, equipment)
6.  GET  /schools/compare?ids=1,2                      → side-by-side comparison view
7.  GET  /locations/{id}/conditions                   → surf conditions widget on location/school pages
8.  GET  /lessons/{id}/slots?from=&to=                → available time slots for a chosen lesson
9.  PUT  /surfer/me/profile                           → save skill level + preferences (onboarding step)
10. POST /bookings                                     → submit a booking request for a chosen slot
11. GET  /surfer/me/bookings?status=                  → "My Bookings" page
12. PATCH /surfer/me/bookings/{id}/cancel              → cancel button (only while PENDING/ACCEPTED, before slot starts)
13. GET  /notifications/me                             → notification bell (poll or on page load)
```

**After a school marks a booking COMPLETED** (server-side, no surfer action needed), a draft progress entry is auto-created:

```
14. GET  /surfer/me/progress                           → "My Progress" list, shows the new draft (draft: true)
15. PUT  /surfer/me/progress/{id}                      → surfer fills in rating/notes, draft flips to false
16. GET  /surfer/me/progress/summary                   → progress dashboard (totals, hours, rating trend chart)
```

A school may later call `PATCH /school/me/progress/{id}/feedback` on that same entry — surfaces as `instructorFeedback` on the entry when re-fetched.

**UI state notes:**
- `GET /surfer/me/profile` lazily creates a default (`BEGINNER`, no preferences) profile on first call — safe to call unconditionally on profile-page mount.
- `POST /bookings` computes `totalPrice` server-side; don't compute it client-side, just display what comes back.
- Booking `status` drives the UI: `PENDING` (show cancel), `ACCEPTED` (show cancel + "upcoming"), `REJECTED`/`CANCELLED` (read-only, show reason/note if present), `COMPLETED` (link to progress entry).

---

## 2. School onboarding & day-to-day operation

```
1.  POST /auth/register/school (multipart)             → creates SCHOOL_ADMIN user + school with status PENDING
2.  POST /auth/login                                    → { token, role: "SCHOOL_ADMIN", schoolId, schoolStatus: "PENDING" }
3.  GET  /school/me                                     → profile page, editable while PENDING
4.  PUT  /school/me                                      → edit profile while awaiting approval
```

**At this point the UI should show a "pending approval" banner and block access to instructors/lessons/slots/equipment/bookings/dashboard** — those all return `403 SCHOOL_NOT_APPROVED` until a platform admin approves the school (see workflow 3). Re-call `POST /auth/login` (or store `schoolStatus` and re-check) to detect the transition to `APPROVED`.

Once approved:

```
5.  POST /school/me/instructors        → add instructor(s) first (slots need an instructor)
6.  POST /school/me/lessons             → add lesson type(s) (slots need a lesson)
7.  POST /school/me/equipment            → add rentable gear (optional, only if lessons offer it)
8.  POST /school/me/slots               → publish bookable time slots (one call per slot; loop client-side for a recurring schedule)
9.  GET  /school/me/dashboard            → operator home screen: pending count, upcoming slots, revenue
10. GET  /school/me/bookings?status=PENDING  → incoming requests inbox
11. PATCH /school/me/bookings/{id}/accept | /reject   → respond to each request
12. PATCH /school/me/bookings/{id}/complete            → after the lesson happens
13. GET  /school/me/progress ... (via booking link)     → PATCH /school/me/progress/{id}/feedback once the surfer's entry exists
14. GET  /notifications/me                               → operator notification feed
```

**UI state notes:**
- Creating a slot requires an existing `lessonId` and `instructorId` from the same school — fetch instructor/lesson lists first to populate pickers.
- `POST/PUT /school/me/slots` returns `409 SLOT_OVERLAP` if the chosen instructor already has a slot overlapping that time range — surface this as a form validation error, not a generic failure.
- `DELETE /school/me/slots/{id}` returns `409` if the slot already has bookings — disable the delete button once `bookedCount > 0` (visible on the slot DTO) rather than waiting for the error.
- `PATCH /school/me/bookings/{id}/accept` can return `409 SLOT_FULL` (someone else filled the last spot) or `409 CONCURRENT_MODIFICATION` (rare, retried server-side already) — show a toast and refresh the list.

---

## 3. Platform admin workflow

```
1.  POST /auth/login                                    → { token, role: "PLATFORM_ADMIN" }
2.  GET  /admin/schools?status=PENDING                  → review queue
3.  GET  /admin/schools/{id}                             → detail view with documents, registration number
4.  GET  /schools/{schoolId}/documents/{index}           → stream/download each verification document
5a. PATCH /admin/schools/{id}/approve                    → approve → school owner notified, can now operate
5b. PATCH /admin/schools/{id}/reject  { reason }         → reject → school owner notified with reason
6.  PATCH /admin/schools/{id}/suspend / /reactivate      → ongoing moderation of already-approved schools
7.  GET  /admin/users?role=&status=&q=                  → user management table
8.  PATCH /admin/users/{id}/suspend / /reactivate        → moderate users
9.  GET  /admin/locations                                 → location management (CRUD)
10. GET  /admin/stats                                     → admin dashboard (counts by role/status/location)
```

**UI state notes:**
- The review queue (`status=PENDING`) is the admin's primary landing view — badge/count it from `GET /admin/stats` (`schoolsByStatus.PENDING`).
- `reject` requires a non-blank `reason` (400 `VALIDATION_ERROR` otherwise) — make it a required form field, not optional.

---

## 4. Booking lifecycle (cross-role, the core transaction)

```
Surfer:        POST /bookings                     → status PENDING, school owner notified (BOOKING_CREATED)
School admin:  PATCH .../bookings/{id}/accept      → status ACCEPTED, slot capacity incremented, surfer notified (BOOKING_ACCEPTED)
   or
School admin:  PATCH .../bookings/{id}/reject      → status REJECTED, surfer notified with reason (BOOKING_REJECTED)
Surfer:        PATCH .../bookings/{id}/cancel       → status CANCELLED (only from PENDING/ACCEPTED, before slot start), capacity released if it had been reserved, school notified (BOOKING_CANCELLED)
School admin:  PATCH .../bookings/{id}/complete     → status COMPLETED, surfer notified (BOOKING_COMPLETED), draft progress entry auto-created
```

Valid transitions only: `PENDING → {ACCEPTED, REJECTED, CANCELLED}`, `ACCEPTED → {CANCELLED, COMPLETED}`. Anything else is a terminal state — hide/disable action buttons accordingly rather than relying solely on the `409 INVALID_STATE_TRANSITION` error.

---

## 5. Notifications (any logged-in role)

```
GET   /notifications/me                 → poll on an interval, or on page focus
PATCH /notifications/{id}/read          → mark individual as read (e.g. on click)
PATCH /notifications/me/read-all         → "mark all read" button
```

Notification `type` values map directly to the events above (`BOOKING_CREATED`, `BOOKING_ACCEPTED`, `SCHOOL_APPROVED`, etc.) — use `type` to pick an icon/route when a notification is clicked (e.g. `BOOKING_*` → booking detail, `SCHOOL_*` → school profile).
