# Gamification review — 9 October 2026

## Sources and team baseline

- Android `origin/develop`: `2043210`; backend `origin/develop`: `19b85df`. Fetch found no newer commits. Existing local changes were preserved. The backend checkout includes the previous merge and XP correction (`0becc284`).
- Report `origin/develop`: `0d07833`. Eleven commits are ahead of the report's local checkout, including mobile wireframes. Reviewed an exported snapshot; the locally edited README was not overwritten.
- Class reference `easyvet-mobile/main`: `c943d85`, fetched again and still current. Domain repositories, application use cases, Hilt bindings, per-screen ViewModels and StateFlow follow this reference. Typed navigation and existing shared shell/components remain in use.
- Live Figma file `nQ8aSe0ovKrqrfQpvV21TC`: medals `228:4181`, ranking `284:4636`, inbox `284:4995`, settings `284:5264`, preferences `284:5298`, language `284:5387`, theme `284:5406`. Inspected design contexts and screenshots. Account/help designs were also inspected (`284:5346`, `284:5425`); their remote account editing, chat and FAQ flows are not implemented in this increment.
- Notion Team Hub refreshed successfully at `2026-10-09T05:15:39Z`, 37 pages. Reviewed accompaniment and notification guidance. Recognition describes participation/learning, never measured environmental impact. The current report/user request retains the team's ranking; the broader Notion scope discrepancy remains open for the team.

## Coverage and remaining work

| Requirement | Current implementation | Remaining |
|---|---|---|
| HU-009 / HU-036: ranking and periods | Existing team screen preserved, local/global/friends/families and period filters; sample-data notice added | Public deployment and complete multi-user production validation |
| HU-011: medal collection | Earned/available tabs, detail, original award time, loading/error/empty/session recovery, account isolation | Broader production catalogue and device evidence |
| TS-016: notification inbox | Local demo inbox, unread badge, read one/all, persisted read status, medal route to collection | Backend delivery, production event targets and lifecycle |
| TS-008: preferences | Six categories matching Figma; saved per account on device; existing history retained | Push transport and server preference sync |
| Language/theme designs | English/Spanish resources and saved selection; light/dark semantic colors in the reviewed flows | Full visual audit of other teams' legacy screens in dark mode |
| Account/help menu | Read-only account summary and informational help | Account email/password edits, support chat, FAQ service |
| HU-003 / HU-008 and domain rewards | Real checkbox completion → server event → points, gems, streak and personal award; history and voluntary Community publication | Other Quests producers, physical device and public deployment |

No screen grants points or medals merely by opening it. The local inbox and medal fixtures are labelled as demonstrations. No push permission is requested because push delivery is not wired. Disabling a category stores a preference; it does not delete historical notifications or claim to stop a production transport. Language/theme are device preferences; notification choices and read status are account scoped.

## Architecture and design adaptations

Notifications and settings follow domain → application → infrastructure → interfaces with Hilt repositories. `ExperienceDataStore` persists their data separately from session storage, so signing out does not erase choices. The root observes settings; screens observe their own state. Domain code has no Android UI dependency. Errors and cancellation are handled separately.

Existing typography, shell navigation, green palette and medal assets are reused. Interactive Material switches/radio controls replace the static Figma drawings, with accessible touch targets. The new shared section heading follows the existing medal header. Native scroll containers accommodate font scaling and small screens. Demo labels and error states extend the design deliberately.

The report's TS-007 uses legacy API names; the backend exposes compatibility routes as well as canonical `/gamification` routes. The integration uses the canonical contracts. The backend's three previously failing Community–Gamification cases were resolved: the local community lookup now compares the enum correctly, and the test matches JWT-derived mutation identity. The full backend suite passed 246 tests. No deployed API URL was verified.

## Academic preflight

The applicable milestone is TB1 under CC238 202620 V4.0, p. 34. The requirements, DDD and recovery techniques/source pages are recorded in `gamification-achievements.md` and `class-pattern-alignment.md`. Before this increment, notification/settings routes were placeholders. This increment replaces the reviewed routes, while explicitly retaining pending transport, production rule and physical-device evidence. It is not a declaration that the academic milestone is ready.

Execution evidence and the updated partial compliance audit are in the course output directory `Trabajo Final/outputs/gamification-2026-10-09`. The recorded runtime demo and validation artifacts are in `/Users/mauriciopajes/Downloads/EcoMind-qa-evidence/2026-10-09`.

## Final runtime checks

The final build passed all 14 JVM tests and all 9 Android instrumentation tests on Medium_Phone (Android 17 / API 37). The instrumentation suite includes actual Activity startup with Hilt, medal collection/detail, inbox callbacks and account-isolated preference persistence. A stale incremental compiler result was resolved with `-Pkotlin.incremental=false`; this flag is recorded in the build log.

Manual checks confirmed English/Spanish switching, preferences surviving a process restart, and Settings → Ranking returning to Ranking. The localized context retains its Activity wrapper for Hilt; system bar contrast follows the chosen theme. Global inbox/settings pages are closed when explicitly selecting a main section; normal section state restoration is preserved.

The final demo is a real, silent ADB screen recording. Its steps are logged in `recording-steps.log`. Video decoding and a contact sheet are checked before delivery. No physical-device or live-API claim is made.

## Real backend integration follow-up

The isolated `GamificationDemoServer` uses an in-memory H2 database and the real application services/outbox. Android signs in with a real test JWT and follows Quests → Gamification → Community. A successful emulator run confirmed 10 ecopoints, 2 gems, a one-day streak, one personal award and a `PENDING` → `PUBLISHED` share. Independent API reads confirmed exactly one reward and one Community achievement post; replaying completion and the same share request did not duplicate either. `gamification-remote-integration.md` describes the implemented adapters, configuration and explicit limits.

Android 17 initially blocked the emulator-host connection without local-network permission. Debug configuration now declares/requests that permission for the development host, and the repeated end-to-end run passed. No production network protection was disabled. Notification fixtures are omitted in remote mode so a real account is not given a simulated unread badge.

The final remote build passed 28 JVM tests. All 10 regression instrumentation tests passed on Medium_Phone (API 37), including persisted share-request identity across repository recreation and account separation. The separate connected flow test passed through the actual Activity and backend. Final APKs and recording remain outside the repository.

The default local mode was also rebuilt and passed all 28 JVM tests. The connected flow was replayed successfully for a real 31-second ADB video, with no seeded rewards. The final H.264 file was decoded and visually inspected; only startup/launcher footage was trimmed and frame timing normalized. Evidence includes the recording, screenshots, test logs and independent API verification. The TB1 audit remains **NO APTO** globally (18 Cumple, 5 Falta, 1 Inconsistencia, 16 Sin evidencia, 11 Riesgo); this bounded-context increment does not override unrelated missing delivery evidence.
