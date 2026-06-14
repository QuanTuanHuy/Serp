# School Bus Route Planning – Design (Post V31 Simplification)

> Updated after Phase 2 backend cleanup. Old greedy/OSRM/timeline/window/trace/issue/score logic has been removed entirely.

---

## 1. Overview

The route planning module follows a **student-centric, manual-first** approach:

1. Admin creates a planning session (school + schedule + direction + date)
2. System shows eligible students (subscriptions matching criteria)
3. Admin manually assigns students to routes and stops
4. Admin publishes session → routes become available for dispatch
5. Dispatch assigns bus + driver + attendant to each route

**Removed in V31:**
- Greedy/automatic route generation algorithm
- OSRM routing engine integration
- N×N routing matrix computation
- Time window validation (pickup/dropoff windows)
- Route calculation trace logging
- Planning issues / blocking issues / quality score
- Objective scoring and optimization weights
- Timeline calculator (planned arrival/departure per stop)

---

## 2. Eligibility Logic

A student subscription is eligible for a planning session when:

| Criterion | Rule |
|-----------|------|
| Tenant | matches session tenant |
| School | matches session school |
| Status | subscription is ACTIVE |
| Date range | `effective_from <= service_date` AND (`effective_to IS NULL` OR `effective_to >= service_date`) |
| Day of week | subscription's day flag is true for service_date's day |
| Direction | OUTBOUND → trip_option IN (MORNING, ROUND_TRIP); RETURN → trip_option IN (AFTERNOON, ROUND_TRIP) |
| Pickup/Dropoff | OUTBOUND requires pickup_point not null; RETURN requires dropoff_point not null |
| Student | active and not deleted |

No time window validation is performed.

---

## 3. Route Structure

Each route (`school_bus_route_plan`) contains:
- Ordered stops (`school_bus_route_stop`) – each linked to a `pickup_point`
- Student assignments (`school_bus_route_plan_student`) – one row per student per route:
  - `pickup_stop_id` → which stop the student boards at
  - `dropoff_stop_id` → which stop the student alights at

Stop types:
- `START_TERMINAL` – route origin (depot or school depending on direction)
- `END_TERMINAL` – route destination
- Middle stops – pickup/dropoff locations

---

## 4. Distance & Geometry (Simple Haversine)

When stops are added, removed, or reordered:
1. System calculates Haversine distance between consecutive stops (in order)
2. Sums total → `planned_distance_km` on the route
3. Builds a simple coordinate array → `geometry_path` (GeoJSON-style `[[lng,lat],...]`)

**Not computed:**
- Road-network distance (no OSRM)
- Planned arrival/departure times per stop
- Travel duration estimates

Frontend can use `geometry_path` to draw a polyline on the map. The distance is straight-line approximation only.

---

## 5. Publish Validation

Before a session can be published:

| # | Rule |
|---|------|
| 1 | Session status must be DRAFT |
| 2 | Session must have at least 1 route |
| 3 | Each route must have at least 1 assigned student |

No blocking-issue validation. No capacity check at publish time (capacity is advisory only).

---

## 6. Capacity

- Each route has a `capacity` field (set when creating the route, typically from bus capacity)
- `studentCount` is maintained as students are assigned/removed
- Frontend should display a warning when `studentCount > capacity`
- Backend does **not** block assignment when over capacity (advisory only in this phase)

---

## 7. Future (Phase 3+)

Features to be rebuilt later:
- Greedy/automatic route generation (new algorithm design)
- Real routing engine integration (OSRM or Google Maps)
- Timeline estimation (ETA per stop)
- Audit logging (new design)
- Pause period management (new design)
