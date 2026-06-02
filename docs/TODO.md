# TODO / Improvement Backlog

Cleanup and improvement items for the Drone Delivery Management Service. Findings
are grouped by area and ordered by priority within each section. Items were
identified by running the app end-to-end against a live PostgreSQL and exercising
the full API (auth, order lifecycle, drone lifecycle, failure recovery, RBAC,
validation, and negative/edge cases).

Legend: **[P1]** must-fix · **[P2]** should-fix · **[P3]** nice-to-have ·
**[x] DONE** addressed

---

## Acceptance Criteria Gaps

Deviations from the assessment's stated acceptance criteria (not polish — these are
where the implementation diverges from what the spec asks for).

- **[x] DONE: Withdraw now rejects orders that have already been picked up.**
  The AC states endusers may "withdraw orders that have **not yet been picked up**."
  `OrderServiceImpl.withdrawOrder` now allows withdrawal only from `PENDING`/`RESERVED`
  (via `validateOrderStatus`); a `PICKED_UP`/terminal order is rejected with a `400`
  business-rule violation. Covered by a new test:
  `OrderIntegrationTest.shouldNotAllowWithdrawingPickedUpOrder`.

- **[x] DONE: Enduser order details now include live drone location and an ETA.**
  `OrderDto` gained `droneLatitude`, `droneLongitude`, and `etaSeconds`. The ETA is
  computed in `OrderMapper` from the assigned drone's current position using the
  great-circle (Haversine) distance and an assumed cruise speed
  (`ASSUMED_CRUISE_SPEED_MPS`): before pickup it covers drone→origin→destination,
  after pickup just drone→destination, and it is `null` for unassigned/terminal orders.
  `status` continues to serve as the order's progress indicator.

---

## Security & Authentication

- **[x] DONE: Return `401` for unauthenticated requests, not `403`.**
  Added `security/RestAuthenticationEntryPoint` returning an RFC 7807 `401` with a
  `WWW-Authenticate: Bearer` header, wired into `SecurityConfiguration` via
  `exceptionHandling(...)`. Authenticated-but-forbidden requests still return `403`.

- **[x] DONE: Stop logging an ERROR + stack trace on every invalid token.**
  `JwtAuthenticationFilter` now logs invalid/expired/forged tokens at `debug` with a
  one-line message instead of `ERROR` + full stack trace.

- **[x] DONE: Don't ship a usable default JWT secret.**
  `application.yaml` now uses `jwt.secret: ${JWT_SECRET}` with **no** default, so the
  app fails fast on startup if the secret is unset rather than booting with a
  publicly-known key. Docker Compose and the test profile supply their own secrets;
  the README documents the requirement for local runs.

- **[P3] Implement token refresh.** *(deferred — larger feature)*
  Tokens are stateless with a 24h expiry and no refresh mechanism. Consider
  short-lived access tokens + refresh tokens. Intentionally left for a follow-up: it
  changes the auth contract and warrants its own design + tests.

---

## API Consistency & Error Handling

- **[x] DONE: Make error responses uniformly RFC 7807.**
  `ExceptionControllerAdvice` no longer emits `"type":"about:blank"`. Validation
  errors now use `https://acme.com/problems/validation-error` (title "Validation
  Failed", with an `instance`) and business-rule errors use
  `.../business-rule-violation` (title "Business Rule Violation"), matching the other
  handlers' custom problem URIs.

- **[x] DONE: Remove the duplicated `message` field on business-rule errors.**
  The redundant `pd.setProperty("message", ...)` was dropped; clients read `detail`.
  Updated `DroneIntegrationTest` to assert on `$.detail`.

- **[x] DONE: Replace raw `PageImpl` serialization in responses.**
  The paginated endpoints (`getMyOrders`, `getAllOrders`, `getAvailableJobs`,
  `getAllDrones`) now return `org.springframework.data.web.PagedModel<...>` (wrapping
  the service `Page`), giving a stable JSON shape and silencing the startup warning.

- **[x] DONE: Cap the maximum page size.**
  `application.yaml` sets `spring.data.web.pageable.max-page-size: 100` (and
  `default-page-size: 20`).

---

## Documentation

- **[x] DONE: Fix the README API reference — routes are out of date.**
  README endpoints now match the controllers (`/api/orders/my-orders`, `/api/orders`,
  `/api/drones/jobs`, `/api/drones/{name}/broken`, etc.) and document the previously
  undocumented drone self-service routes (`/api/drones/broken`, `/fixed`,
  `current-order`), the `fail` route, and `PUT /api/orders/{id}/destination`.

- **[x] DONE: Document the JDK 21 requirement and local build setup.**
  README "Prerequisites" now calls out the enforced JDK 21 (`JAVA_HOME` + the
  `maven-enforcer` `requireJavaVersion [21,22)` rule) and the now-required `JWT_SECRET`
  environment variable (with an example in the local-run step).

---

## Build & Tooling

- **[x] DONE: Enforce Java 21 in the build.**
  Root `pom.xml` uses `maven.compiler.release=21` + `java.version=21` and a
  `maven-enforcer-plugin` `requireJavaVersion [21,22)` rule. The leaf module's
  compiler plugin uses `<release>21</release>`. CI provisions JDK 21.

- **[x] N/A: Redundant Hibernate dialect setting.**
  No explicit `hibernate.dialect` property exists in any config file
  (`application.yaml`, `application-docker.yaml`, `application-test.yaml`) — nothing to
  remove. The `HHH90000025` warning is no longer applicable.

- **[x] DONE: Remove the private Nexus repo from `services/drone-delivery/pom.xml`.**
  The `<repositories>` block pointing at `https://nexus.acme-solutions.com/...` was
  removed so builds resolve from Maven Central and work outside that network.

- **[P2] Testcontainers can't reach Docker Engine 29.x locally.** *(environment, not code)*
  Integration tests fail locally with `Could not find a valid Docker environment` /
  `Status 400` because the Testcontainers docker-java client can't complete the
  `/info` handshake with Docker Engine 29.5 (the `docker` CLI works fine; CI passes).
  *Options:* enable the `tcp://localhost:2375` daemon endpoint (or the legacy
  `//./pipe/docker_engine` named pipe) for local runs, or track a Testcontainers
  release that supports Docker 29. Not a code defect — environment/version compat.

---

## Notes

The core service is solid: the full happy path (auth → create → reserve → pickup →
deliver), failure recovery (broken drone resets its order to `pending` and relocates
a picked-up order to the drone's last position), RBAC, ownership checks, and input
validation all work correctly when verified against a live database.

All code/doc items above have been addressed except **token refresh** (P3, deferred
as a standalone feature) and the **Testcontainers/Docker 29 local compatibility**
issue (an environment limitation, not a code defect — CI runs the suite). The
remaining changes were verified to compile (`mvn test-compile`) under JDK 21; the
integration suite runs in CI where Docker is available.
