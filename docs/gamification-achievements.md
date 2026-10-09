# Personal medals and achievements

> This document records the initial medal increment. The current review, notification/settings work and validation are in [gamification-review-2026-10-09.md](gamification-review-2026-10-09.md).

This increment replaces the Profile → Medals and achievements placeholder with a personal collection and a read-only detail screen. It addresses **HU-011, scenario 2**. Awarding medals (scenario 1) remains a backend responsibility: opening these screens never grants or recalculates rewards.

## Implemented behavior

- Earned medals and available definitions have separate tabs, defaulting to the person's earned collection.
- Selecting a medal opens its description, criterion, status and original award timestamp in UTC.
- Profile remains selected in the shared navigation; back returns to the collection/profile.
- Loading, empty collection, failed reads, expired/missing session and missing detail have distinct states. Failed reads offer retry; an expired session routes to sign-in.
- Only individual awards belonging to the current authenticated account appear. Duplicate rows are removed. Archived earned definitions remain in history, while inactive unearned definitions are hidden.
- An award with a missing definition produces an error rather than silently hiding part of the collection.
- English and Latin American Spanish resources are externalized. Existing shell, typography, buttons and colors are reused.

## Data boundary

The application currently uses `LocalAchievementRepository`, consistent with its existing demo authentication. A persistent **Demo** notice appears on collection and detail. The seeded student has two fictional awards; other demo accounts have an empty earned collection. Names, thresholds, timestamps and criteria in these fixtures are examples, not approved production rules or environmental measurements.

`AchievementRepository` separates UI/application behavior from a future remote adapter. `AchievementDto` and `AchievementAwardDto` match `AchievementResource` and `AchievementAwardResource` in backend develop, inspected at `40f7d56`:

| Read | Contract |
|---|---|
| Catalog | `GET /api/v1/gamification/achievements?scope=INDIVIDUAL&page=&size=` |
| Personal awards | `GET /api/v1/gamification/me/achievements?page=&size=` |
| Definition | `GET /api/v1/gamification/achievements/{achievementId}` |

Catalog and personal awards return arrays, unlike the ranking page envelope. A remote repository must gather every page (zero-based, maximum size 100) and include archived definitions needed by earned awards. Identity comes from the JWT; the client must not supply another user's identity to the `/me` route. This increment does **not** implement HTTP or claim API integration. A deployed URL and real IAM session are still needed.

Backend deployment check on 2026-10-08: the latest `origin/develop` is `40f7d56`. The README documents local port 8092; the production profile reads `PORT` and database/email settings from environment variables. There is no public base URL or deployment manifest in the tracked repository, and GitHub returned no deployments or Actions runs. This does not rule out an externally managed deployment, but no usable public endpoint was verified. The existing local backend branch was left untouched.

Sharing, family/community collections, reward history, progress/streak screens and server-side granting are outside this increment. The existing ranking remains unchanged. No production economy, granting formula or environmental impact claim was added.

## Design and source preflight

The current Figma was read live on 2026-10-08. Reference: [51 · My medals · HU-011](https://www.figma.com/design/nQ8aSe0ovKrqrfQpvV21TC/?node-id=228-4181). Its title, row structure, typography and medal assets are preserved through the application's reusable components. The available tab, detail and recovery states extend the read flow; they do not assert that the original Figma was complete. The two PNG assets were downloaded unchanged from the source image fills and are stored in `drawable-nodpi`.

Product authority: EcoMind Team Hub's Product definition, Product evolution, Modules and kits and Journey §17, inspected from the snapshot refreshed on 2026-10-08. Gamification recognizes participation and learning and does not certify impact. The existing ranking/store scope discrepancy identified in the app review is not resolved by this change.

| Artifact | UPC technique/source | Finding before work | Correction |
|---|---|---|---|
| HU-011 acceptance | Requirements review kit, `SI397_S10_MAD_USER STORIES AND ACCEPTANCE CRITERIA.pdf`, pp. 5–7 | **Falta:** Profile's collection route was a placeholder. | Implement the collection and check access/navigation with acceptance-focused tests. |
| Layer responsibilities | Design review kit, `upc-pre-si424-ea-domain-driven-design-part-3_v1.pdf`, slides 4 and 18 | **Cumple** for existing context separation; no achievement reader existed. | Add models/repository/use case and keep granting outside UI. |
| Recovery | UX review kit, `SI385_S3_MAD_USABILITY_V1.pdf`, pp. 5–7 | **Sin evidencia:** no collection/error/missing states. | Implement distinct states, retry, back and reauthentication. |

The applicable normative source remains CC238 202620 V4.0, p. 34 (TB1). Source presence, successful tests and an emulator run do not establish completion of the entire academic milestone or physical-device validation.

## Class patterns

The class reference was updated to `easyvet-mobile/main` at `c943d85a74d3b41fc446cd16f501c2434f653899`. The alignment, source files and deliberate adaptations are recorded in [class-pattern-alignment.md](class-pattern-alignment.md). Collection/detail now have separate ViewModels and states, and the detail is loaded by its own ID-based use case.

## Verification

`AchievementsTests` covers earned/available separation, repeated awards, account/scope isolation, archived history, empty data, expired/missing session, read failures and missing definitions. `AchievementsScreenTests` covers HU-011 navigation callbacks/tab filtering, failure recovery and missing detail. Run:

```sh
bash ./gradlew :app:assembleDebug :app:testDebugUnitTest
bash ./gradlew :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=pe.greenminds.ecomind.gamification.AchievementsScreenTests
```

Before the class-pattern refactor, executed on 2026-10-08: `:app:testDebugUnitTest :app:compileDebugAndroidTestKotlin` succeeded (7 achievement tests and the existing example test, zero failures/errors). `assembleDebug` and `assembleDebugAndroidTest` also succeeded. Both APKs were installed in the Medium_Phone emulator; the AndroidJUnitRunner execution completed with `OK (3 tests)` for the achievement UI suite. The real application flow was also exercised: demo sign-in → Profile → View all → collection → earned medal detail → Back → collection. Collection/detail screenshots were visually inspected; the Profile navigation stays selected, text is readable and source medal assets render correctly. All 31 new resource keys have Spanish counterparts. Physical-device validation and live API integration remain pending.

Actual execution results and the partial TB1 compliance audit are recorded separately in the local course output directory `Trabajo Final/outputs/app-status-2026-10-08/`. Pending compliance reviews and automatic failures are retained; this increment does not declare TB1 ready.

After alignment with the latest class code, this revision passed the full APK/unit build and all 9 achievement unit tests (plus the existing example). Both updated APKs installed in Medium_Phone, and AndroidJUnitRunner reported `OK (4 tests)`, including detail loading through the screen, dedicated ViewModel and use case. Current execution logs and partial TB1 audit are in `Trabajo Final/outputs/class-pattern-alignment-2026-10-08/`. See the class-pattern mapping for the compiler diagnostic and its resolution.
