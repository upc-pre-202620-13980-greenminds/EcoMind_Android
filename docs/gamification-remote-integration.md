# Gamification integration

## Scope and source of truth

This increment extends the team's existing ranking, medal collection and shared Android shell. It follows the current `easyvet-mobile` class reference (`c943d85`): domain repository ports, application use cases, Retrofit adapters, Hilt injection, per-screen ViewModels, StateFlow and typed navigation callbacks.

The current report (`0d07833`, Gamification bounded context) defines XP as ecopoints. Android shows one ecopoints total; it does not introduce an XP ledger. Points recognize participation and learning, not measured environmental impact. Live Figma nodes `228:4181`, `20:645` and `47:3674` informed the medal and activity flow. Additional progress/history/share states adapt the existing components to the actual service contract.

## Implemented coverage

| Flow | Implementation and authority |
| --- | --- |
| Ranking | Existing UI and period filters; remote participants/transactions, full pagination and strict timestamp mapping. The server determines authorized local, global, friends and family results. |
| Personal progress | Server ecopoints, current/longest streak, last activity and protected day. No client-side reward calculation or streak protection command. |
| Reward history | Granted rewards, source and event time; daily/weekly/monthly/all-time UTC filters. Base reward is not substituted for the granted amount. |
| Personal medals | Remote catalog and current-account awards; earned/available collection, detail and original award time. |
| Collective medals | Family/community memberships come from the authenticated server; lists use the corresponding award endpoints. Community awards use `communityId`, with nullable individual `beneficiaryId`. |
| Voluntary sharing | Explicit community selection and confirmation. Persist a stable request ID before POST. Restore/retry uncertain requests; show `PENDING` until Community confirms `PUBLISHED`. Never publish on opening a medal. |
| Activity → reward | Daily/activity checkbox execution uses Quests start, submit and finish endpoints. Gamification consumes the backend event; Android reloads progress and awards. Reopening/completing an already finished activity does not issue another finish command. |
| Session/data isolation | Real sign-in and JWT, profile/family/friends adapters, rejection of demo tokens in remote mode, and rejection of responses received after account switching. HTTP 401 shows sign-in recovery. No silent fallback to fixtures. |

## Configuration

The existing local demonstration remains the default. Explicit remote mode selects all related remote repository adapters:

```sh
bash ./gradlew :app:assembleDebug \
  -Pecomind.remote=true \
  -Pecomind.apiBaseUrl=http://10.0.2.2:8092/api/v1/
```

Use the team's existing backend with the appropriate API URL and a valid account. No additional server or seed fixture is included in either PR. A deployed URL can be supplied with the same property; it must end in `/`. Release uses HTTPS and still needs the team's deployed host. The debug manifest alone permits HTTP and local-network access. Android 17 requires local-network permission for the emulator host; debug builds request it when configured for `10.0.2.2`. Automated emulator runs can grant it with:

```sh
adb shell pm grant pe.greenminds.ecomind android.permission.ACCESS_LOCAL_NETWORK
```

Reference: [Android local-network permission](https://developer.android.com/privacy-and-security/local-network-permission).

## Explicit boundaries

- The backend remains authoritative for reward multipliers, deduplication, daily streak closure and Monetization protection entitlement.
- Quests currently provides an executable checkbox handler. Android does not invent quiz, photo, minigame or collaborative execution results. Those producers require their own completed flows before they can supply Gamification events.
- Group awards are read from the backend. An empty group collection is valid and does not create demo awards.
- Notifications/preferences from the earlier increment remain clearly labelled local demonstrations; there is no server notification transport in the inspected backend.
- A public deployment, physical Android device, all activity producers and full-app academic acceptance are not certified by local integration tests.

## Validation

See `gamification-review-2026-10-09.md` for final test results and real emulator evidence. `RemoteGamificationTests` exercises HTTP contracts, pagination, authorization, actual granted values, collective ownership, cancellation and share retry behavior. The earlier connected Activity/outbox walkthrough was executed with a local QA fixture, kept outside the repositories. Its fixture-specific instrumentation test is not part of the PR; the repository retains the 10 regression instrumentation tests and 28 JVM tests.
