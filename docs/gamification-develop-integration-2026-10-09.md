# Gamification integration with current develop

Scoped review, 2026-10-09. Android feature head before integration: `3f9d31c`;
team `develop`: `6db2af6` (21 incoming commits). Backend `develop`: `11556d0`.
Class reference: `easyvet-mobile/main`, `c943d85`.

## Preflight

The current 202620 assignment and the class implementation remain authoritative.
The design references and class sources were already inspected in
`class-pattern-alignment.md` and `gamification-structure-review-2026-10-09.md`.
This review applies their domain ports, infrastructure adapters, application
queries, Hilt composition, typed navigation and screen-specific StateFlow pattern
to the updated team implementation. It does not certify the whole TB1 delivery.

| State | Rule/source | Evidence | Gap and remediation |
|---|---|---|---|
| Inconsistency | One shared HTTP composition boundary; class Retrofit/Hilt pattern | Both branches add `shared/infrastructure/di/NetworkModule`; develop includes IAM `AuthInterceptor` and per-context API modules. | Retain the team's authenticated client and move Gamification/Users API bindings into their own modules. |
| Inconsistency | Preserve team context ownership; class domain repository pattern | Develop adds `quests/application/service/QuestExecutionService`, entity/value objects and execution presentation; feature has another execution model and screen. | Retain develop's complete Quests flow and remove the superseded feature adapters/screens. Gamification reads rewards awarded by the backend. |
| Inconsistency | Preserve integrations from develop | Develop adds `GemBalanceStore` and Monetization/Profile cosmetic integration; feature shell adds unread notifications. | Combine both behaviors; preserve Quests navigation and Community content. |
| Risk | Current-account isolation | Gamification originally captured a session and explicitly authorized requests; develop's interceptor reads the current session. | Centralize Authorization in the shared IAM interceptor and reject a captured request if its session changed. Test this interaction. |
| Inconsistency | Remote data must not silently become demo data | Develop IAM/Quests/Monetization use remote adapters; feature default selects demo Gamification and Users. | Make the standard build remote and retain demo only as an explicit development choice. Preserve the team's release endpoint and dependencies. |

## Validation

- Integrated all 21 incoming commits from `6db2af6`; fetched again before closing
  the review and `origin/develop` still points to that commit.
- Preserved develop's IAM API/mapper/remote implementation, shared authenticated
  client, Quests entities/services/adapters/screens, Monetization implementations,
  `GemBalanceStore`, Profile cosmetics and Community awards/news. The Quests diff
  against develop only adds the completed-screen callback and navigation to
  Gamification. There is no remaining duplicate quest execution model or screen.
- Added context-specific Gamification/Users API modules using the team's shared
  Retrofit. Removed duplicate Retrofit dependency aliases and the feature's
  superseded authentication adapter. Standard builds select remote data; release
  always uses remote adapters and keeps the team's configured host.
- Kept the team's gem balance observer and added the existing notification count
  alongside it. Returning from a completed quest reloads server balances. Ranking
  and personal medals refresh on destination resume.
- Centralized Authorization in IAM and rejected ranking results assembled across
  account changes. Moved the shared session failure into the shared domain
  so Users' remote access no longer depends on Gamification's domain.
- Implemented the new `ProfileRepository.spendGems` port as a failure in the remote
  adapter: no standalone debit endpoint exists. The team's remote Store adapter
  purchases through Monetization, which remains the authority for wallet debits.
- `assembleDebug`, `assembleRelease`, `testDebugUnitTest` and
  `connectedDebugAndroidTest` passed. **46 JVM tests passed**; **13 emulator tests
  passed**, with the opt-in real-backend connectivity test skipped because no
  backend fixture/credentials were supplied. The device was `Medium_Phone`,
  Android 17/API 37. The Quests UI regression also checks the rewards callback.
- An earlier smoke-test run was interrupted by the OS local-network permission
  dialog. The test now grants that permission before launching MainActivity on
  API 37+, following the permission API already used in develop's connectivity
  test. MainActivity retains the team's permission request after `setContent`.

The native builds validate Hilt composition, domain contracts and UI regressions.
They do not certify a public deployment, a physical device or a fresh end-to-end
run against the backend. Backend `11556d0` and the report's existing uncommitted
README changes were preserved. No temporary backend server is included.

## Academic audit status

Ran the required strict TB1 audit against the local Android checkout, using a
copied manifest with current local paths. The original course manifest was not
modified. Result: **NO APTO** for the overall delivery gate. Deployment metadata,
physical-device/full-delivery evidence and other products remain outside this
integration's evidence. The `review_queue` remains open for those artifacts.

`AND-006` also reports missing Spanish resources because the checker looks for a
literal `strings.xml`; the team's Latin American Spanish directory contains
split `strings_*.xml` files, including Gamification and the new rewards label.
This naming limitation is documented without overriding the automatic failure.
The rule needs a separately reviewed checker correction or delivery remediation
before academic acceptance can be asserted.

Build logs, test XML and the audit output are preserved in the local course
directory `Trabajo Final/outputs/develop-integration-2026-10-09/`.

## Connection parity follow-up

Re-fetched Android, backend and class references: `develop` remains `6db2af6`,
backend remains `11556d0`, and EasyVet remains `c943d85`.

The API modules already used the same singleton Retrofit and API base URL as
Quests/Monetization. The remaining difference was passing a bearer token through
each Gamification/Users API method. Those explicit `@Header` parameters are now
removed. **Only `iam.infrastructure.remote.AuthInterceptor` constructs the
Authorization header in production code.** Its normal behavior remains the same
for the team's untagged IAM/Quests/Monetization requests.

Gamification/Users attach their captured Session using Retrofit's internal
`@Tag`. This is client-only metadata: it is not a header, query parameter or JSON
payload. The shared interceptor checks it against the current session and blocks
the request before transport on account switching, logout or expiration.
`RemoteAccess` handles current-account validation and Result/recovery; it no
longer formats or supplies a token. Post-response checks remain in place.

`SharedConnectionTests` invokes the real NetworkModule and four feature API
modules through one Retrofit/client. A test-only transport redirect captures the
wire requests after the IAM interceptor. It verifies the configured `/api/v1`
paths, exactly one common Authorization header for all four contexts, decoded
responses and no session data in GET bodies. The existing HTTP contract tests
now use this shared client too. A queued account-switch test confirms that no
request reaches the transport with the wrong account's token.

Debug/release builds and **49 JVM tests passed**, including the shared-connection
and session-isolation tests. No live-backend or physical-device claim follows
from these contract tests. The strict TB1 auditor was re-run; its delivery-wide
NO APTO status and the documented localization-checker limitation remain open.

A new instrumentation run built its test APK but could not execute: no device
was connected and the AVD refused to start for insufficient disk space (163 MiB
available). The earlier 13 passing emulator tests belong to the previous
integration revision. Logs for this attempt are saved alongside the new JVM
results; no files outside this task were removed to free space.
