# TajsOS code quality audit

Reviewed **16 October 2026**, on `dev` at `92b90a607336d07857c3fc2f6e1ed061658aceee`.

## Assessment and scope

The existing KMP architecture is worth retaining. Shared commands, Room transactions, typed companions, the shell/scaffold system, and `TaskRow` already provide useful consolidation. The main quality problems are incomplete ownership boundaries, obsolete UI implementations left alongside their replacements, and duplicated forms/layout machinery. Some of those problems cause observable persistence errors; others are maintenance costs and should receive lower priority.

The highest-value work is to protect edits and aggregate writes first: stale full-row note saves can overwrite another edit (**F27**); Track/Review orchestration still spans separate transactions (**F04**); deletion can leave dangling membership pointers (**F08**); and Today queries do not advance their captured date during a long-lived subscription (**F28**). Avoid starting with a broad rewrite or indiscriminate duplicate removal.

This review inventoried **404 Kotlin files** under the four modules' `src/` trees: **305 production files / 66,572 lines**, plus 99 test files. Production breakdown: `shared` 66, `composeApp` 235, `androidApp` 1, `server` 3. These are physical source counts, including comments and platform variants, excluding generated/build output. Inventory and text scans covered those roots; detailed manual inspection sampled persistence, commands, reactive state, navigation, Notes, Profile/Settings, Track, Protocols, domain lenses, layouts, Android share handling, server routes, and build/workflow configuration. It was not a line-by-line review of every screen, translation, platform, or dependency.

The previous audit is preserved unchanged as [`../archive/AUDIT.1.md`](../archive/AUDIT.1.md), SHA-256 `3ce3ed0ae744a3a89d4aa32cf640f85ac5746f1466b879258ac5998be112714a`. Its attachment provenance, historical results, and detailed proposals remain there. This document replaces its active backlog. **F01–F26 retain their IDs**; F27–F35 are new ledger entries. F35 promotes an unresolved observation from the previous audit's route narrative. SP01–SP05 remain subfindings under their original parents, not additional backlog items.

Evidence labels:

- **Reproduced**: a disposable real-Room characterization probe demonstrated the stated behavior. This does not establish UI/device reproduction.
- **Source verified**: the implementation/call path is present; no runtime claim is implied.
- **Decision / acceptance gate**: a contract or platform check remains open; it is not automatically a defect.
- **Resolved mechanism / partial**: the old specific bug is repaired, while explicitly named limitations remain.

Priority is independent of ID: **P1** protects stored data or core behavior; **P2** affects product consistency or substantial maintenance; **P3** is bounded cleanup or a conditional capability. The prior audit's **G0** durability/privacy gates are retained as gates, without implying a newly reproduced production incident.

Source shorthand below: **S** = `shared/src/commonMain/kotlin/com/tajemniktv/tajos/`; **C** = `composeApp/src/commonMain/kotlin/com/tajemniktv/tajos/`; **A** = `androidApp/src/main/`. Line references describe the audited commit.

## New findings

### F27 — P1: Notes autosave replaces whole stale node snapshots

**Reproduced at the repository boundary; UI trigger source verified.** Both narrow and wide Notes editors save title/content changes with `selectedNote.source.copy(...)`. `NodeCommands.updateNode` launches a separate coroutine and eventually replaces the entire row. Reading the current row there only helps calculate postpone metadata; it does not merge the changed field. `AppRepository.updateNode` wraps the replacement in a transaction, which cannot recover fields already stale in the supplied value.

Evidence: [NotesRoute.kt:227](../../composeApp/src/commonMain/kotlin/com/tajemniktv/tajos/ui/screens/notes/NotesRoute.kt#L227), also lines 287–303; [NodeCommands.kt:293](../../shared/src/commonMain/kotlin/com/tajemniktv/tajos/ui/main/actions/NodeCommands.kt#L293); `S/data/Repository.kt:845–849`. The editor's displayed text is fed from the persisted note (`C/ui/screens/notes/NotesEditorPane.kt:114–155`), without a local editing model that serializes these patches.

A Room probe loads one rendered note, saves `rendered.copy(title = "Edited title")`, then saves `rendered.copy(content = "Edited body")`. The final title is **Original**, while the body is updated. No concurrency is required to demonstrate stale replacement. Rapid callbacks before a new rendered snapshot, or independent edits to another field, can reach that same mechanism. Physical typing/recomposition behavior was not measured.

**Recommendation:** add field-specific repository commands that reload and patch within one transaction, and give the editor an explicit draft/save/error model. Serialize or coalesce body saves while retaining the newest draft; cancellation must not discard the final edit. Reuse these commands across existing note surfaces. Merely adding a delay/debounce to the existing stale full-row replacement does not solve the overwrite.

**Acceptance:** title/body edits from one stale render preserve both fields; a concurrent pin/assignment survives a body save; switching notes cannot save a draft to the wrong ID; failed saves retain recoverable text. Existing lifecycle-specific commands already illustrate the reload-and-patch approach.

### F28 — P1: Today projections retain yesterday's query after midnight

**Source verified.** [TodayPinProjection.kt:25](../../shared/src/commonMain/kotlin/com/tajemniktv/tajos/data/TodayPinProjection.kt#L25) resolves the date once when collection starts, then observes pins for that fixed string. `S/data/Repository.kt:345–352` captures a date when building `getTodayNodes()`, and line 1282 similarly captures the date for `isPinnedToToday`. `S/ui/MainViewModel.kt:211–214` eagerly keeps `allNodes` collected for the ViewModel lifetime.

A running Desktop session can therefore keep observing the previous day's pins even when subsequent node/Room emissions occur. A new subscription fixes the date, so fresh-subscription historical-pin tests do not cover this case. Device timezone changes have the same invalidation problem. Date-sensitive calculators also depend on their upstream emissions; injecting reference instants into calculators has not itself made the live graph tick.

**Recommendation:** supply a shared, testable local-date signal and switch the affected queries when its date/timezone changes. Re-evaluate on app resume as appropriate. Reuse one signal rather than separate ad hoc timers per screen.

**Acceptance:** keep the same subscriber alive across midnight and timezone changes; yesterday's pins disappear and today's appear without restarting, navigating away, or writing a node. Also check the Today membership/read models together.

### F29 — P2: Superseded Notes implementations remain in production source

**Source verified maintenance issue.** `C/App.kt:392,404` calls the responsive `NotesRoute`. In contrast, source-wide symbol checks found only the declaration for `NotesWorkspaceDetail` ([file:103](../../composeApp/src/commonMain/kotlin/com/tajemniktv/tajos/ui/screens/notes/NotesWorkspaceDetail.kt#L103)) and `NotesDashboardBlockRegistry` (`NotesDashboardBlocks.kt:53`). The old registry points to `NotesMainBlock`, but has no source consumer. Its associated `NotesDashboardContracts.kt` and `NotesDashboardLayoutEngine.kt` form another unused layout path. `NotesScreen.kt:17` is also an unreferenced wrapper around the active route.

These leave multiple filters, list/editor renderers, and node-action implementations available for accidental maintenance of the wrong surface. The unused workspace is substantial, not just a harmless forwarding function. Source absence is the evidence; a minifier's treatment of this code was not measured.

Related P3 cleanup candidates also have declaration-only source matches: `C/ui/components/layout/DashHeader.kt:50`, `HeaderComponents.kt:64,106` (`SystemOnlineStatus`, `DesktopSearchSurface`), `C/ui/components/common/AbortSlider.kt:42`, and `S/data/AppDatabase.kt:168` (`getDatabaseBuilder`). The live shell uses its newer header path, and platform database builders configure Room directly. Confirm intended API/preview use individually before deletion; these extra examples are not separate behavioral defects.

**Recommendation:** remove the isolated old workspace/dashboard path after checking its helper symbols and previews individually. Transfer any still-desired behavior into `NotesRoute`; do not revive the whole old implementation. **Keep `notes/detail/NoteDetailScreen`**: App, Task detail, and Record detail still call it. The Notes route and this active detail surface require a shared edit contract, not blind deletion of both.

**Acceptance:** application routes still open Notes and canonical item IDs; Record/Task note delegation works; Notes filters, attachments, and editing keep their current behavior. No direct or indirect source references should remain to deleted helpers.

### F30 — P2: Profile and Settings duplicate the medication form and disagree on validation

**Source verified behavior divergence.** [ProfileRoute.kt:190](../../composeApp/src/commonMain/kotlin/com/tajemniktv/tajos/ui/screens/profile/ProfileRoute.kt#L190) and [SettingsDashboardBlocks.kt:743](../../composeApp/src/commonMain/kotlin/com/tajemniktv/tajos/ui/screens/settings/SettingsDashboardBlocks.kt#L743) separately implement substance, brands, dosage, hour, optional state, and the save button. Settings disables Save for a blank substance at line 801; Profile's button at lines 243–253 has no such condition. `S/ui/MainViewModel.kt:1893–1908` accepts the supplied values without validation. Both dialogs parse an arbitrary integer hour without a 0–23 bound.

**Recommendation:** use one localized medication editor and one domain-level validation contract. Preserve the two legitimate entry points. Validate required substance and the hour range before persistence; distinguish an absent hour from invalid input rather than silently converting invalid text to null.

**Acceptance:** Profile and Settings accept/reject the same inputs, including whitespace substance, blank hour, nonnumeric hour, -1, 0, 23, and 24. No blank medication row is persisted through another caller. UI/device interaction remains untested in this audit.

### F31 — P3: Android share handling contains two implementations of each Parcelable helper

**Source verified redundancy.** [MainActivity.kt:502](../../androidApp/src/main/kotlin/com/tajemniktv/tajos/MainActivity.kt#L502) and line 516 define member reified helpers. Lines 532 and 550 define top-level class-parameter helpers with the same `IntentCompat` calls and exception handling. The actual image-share callers at lines 261 and 281 use the class-parameter versions; a source search found no call to the member versions.

**Recommendation:** keep one implementation per operation. Remove unused member helpers, or make a needed convenience overload delegate to the existing implementation. Keep malformed parcel handling and secret-safe diagnostics. Do not collapse single-item and multi-item return types merely to reduce the count.

**Acceptance:** single/multiple image shares retain behavior and malformed parcelables still fail safely. Android device share-sheet acceptance was not performed.

### F32 — P3: Screen layout scaffolding repeats equivalent surface branches

**Source verified maintenance issue; no distinct Desktop content loss established here.** There are 36 `*LayoutEngine.kt` files under common UI. For example, Health, Identity, Track, and Time Architecture repeat the same block list for MOBILE and DESKTOP. Their feature contracts repeat surface/block/plan structures. Six routes hardcode MOBILE with “Default for now”: Identity, Review, Rules, Templates, Track, and Time Architecture.

Evidence: `C/ui/screens/{health,identity,track,timearchitecture}/*LayoutEngine.kt`; `IdentityScreen.kt:49`, `ReviewScreen.kt:99`, `RulesScreen.kt:66`, `TemplatesScreen.kt:81`, `TrackScreen.kt:120`, `TimeArchitectureScreen.kt:51`. `C/ui/components/common/ScreenBlockLayout.kt:26` already offers a small shared planner but has no caller.

**Recommendation:** remove equivalent branches or adopt the existing small shared planner where it actually reduces code. Keep feature-specific context and richer layout overrides where they earn their keep. Do not introduce another framework or assume all 36 engines are interchangeable. Select width-based surfaces when a feature truly has distinct layouts.

**Acceptance:** preserve block IDs, order, overrides, and compact navigation. Identical plans alone do not prove a responsive-layout bug; measure the actual layout before prescribing a redesign.

### F33 — P2: Active Notes UI bypasses localization resources

**Source verified.** Examples include `C/ui/screens/notes/NotesListRail.kt:107` (`Search notes...`), `NotesEditorPane.kt:125` (`Untitled note`), and labels/actions in that pane and `NotesContextPane.kt`. `notes/detail/NoteDetailScreen.kt:203,210` builds `Unassign` labels directly. These are visible strings in active paths, not persisted protocol keys. Localized surrounding components and `values/` / `values-pl/` resources already exist.

**Recommendation:** move these strings into the existing Compose resources, including pluralized counts and accessible action names. Review generated captions from shared calculators separately: presentation should map typed states to localized labels rather than translate stored identifiers. Avoid duplicating another English string map.

**Acceptance:** core Notes list/editor/context and assignment dialogs consistently use the selected locale. Keep storage keys, imported user text, and item identity unchanged. A full translation audit was not performed.

### F34 — P2: Root state and persistence still concentrate unrelated responsibilities

**Source verified architectural debt; file length is supporting evidence, not a bug by itself.** `S/ui/MainViewModel.kt` is 2,852 lines; `main/calculators/MainSnapshotCalculators.kt` 2,879; `data/Repository.kt` 1,995. The ViewModel mixes search filter state, settings, health writes, focus sessions, reviews, exports, and a chain of identity/direction/capacity snapshots. Existing `main/actions`, `main/state`, and `main/calculators` extractions are useful, but Track and Review aggregate orchestration remain in the root (F04), while note edits lack field ownership (F27).

`C/ui/screens/dashboard/DashboardScreen.kt:290–320` and `DashboardBlockContracts.kt:22+` also repeat a large dependency bundle, passed again through the renderer. This is real repeated wiring, but the independent context types are not duplicate business implementations merely because their fields match.

**Recommendation:** continue the existing incremental extraction: first move transactional application operations behind repository/application commands; then separate coherent feature state ownership while retaining the root facade where useful. Reduce repeated dashboard arguments around an existing context contract. Avoid a wholesale ViewModel/repository replacement or generic “service” abstraction.

**Acceptance:** preserved observable contracts and targeted command/read-model tests, followed by the shared/Compose suite. No performance or defect-rate improvement is claimed from splitting files alone.

### F35 — P1: Ending a Protocol session discards its entered notes

**Source verified; carried from the previous audit's unnumbered Protocol observation.** `C/ui/screens/protocols/ProtocolsScreen.kt:224` stores notes in `rememberSaveable`. The active run passes this text only to the input at lines 304–305/919–924. End session invokes `onSkipToLibrary` (lines 691,731,884), whose handler at [line 314](../../composeApp/src/commonMain/kotlin/com/tajemniktv/tajos/ui/screens/protocols/ProtocolsScreen.kt#L314) clears `sessionNotes` without a persistence call. `S/ui/main/actions/ProtocolCommands.kt:59–104` records trigger history with generated source text, not the entered session notes.

**Recommendation:** persist notes to the existing session/history lineage before clearing the editor, with completion/failure feedback. Decide the correct existing record owner; do not create a second protocol history store. If notes are intentionally temporary, the UI must say so before users enter them.

**Acceptance:** type notes, end a session, reopen its history, and recover the exact text. A failed save must retain the draft. Also check switching sessions and restart. No UI run was performed for this source-confirmed path.

## Earlier findings carried forward

These are the active remaining portions of F01–F26. Policy questions, hypotheses, and deferred capabilities are separated from defects. Closed mechanisms are listed afterwards so they are not repeatedly rediscovered.

| ID | Current disposition and evidence | Remaining action / acceptance |
| --- | --- | --- |
| **F01 — backup credentials/privacy** | **Partial; G0 privacy gate.** Token redaction/import identity handling remains repaired (`S/data/ExportBundle.kt`, `BackupDao.kt`). Private provider URLs remain in unencrypted JSON. | Describe exports as private recovery files; decide handling of credential-bearing feed URLs and at-rest credentials. No claim that exports are safe to share. SP01 remains closed. |
| **F02 — backup completeness/portability** | **Partial; G0 recovery gate.** Schema 4 covers Room tables and explicitly encodes the version; Settings retains the full export. External attachment bytes, URI grants, DataStore settings, and unrelated-store ID remapping remain outside it. | Publish/test the portability boundary or a manifest; test real file recovery and current device clipboard/import behavior before promising whole-profile recovery. SP02 remains closed. |
| **F03 — destructive migration/reseeding** | **Open; G0 durability gate.** All three platform `Database.kt` builders retain `fallbackToDestructiveMigration(true)`; Room version is 35. `main/bootstrap/AppBootstrapper.kt:43–45` seeds example nodes whenever none exist. | Decide disposable pre-alpha versus durable-data policy. Test a populated upgrade, unsupported-schema recovery, and intentional empty-profile restart. Do not incidentally change release/data policy during cleanup. |
| **F04 — aggregate transaction coverage** | **Partial, P1; prior closure narrowed by this review.** Node/triage/pin/backup/calendar/saved-view transactions remain valid. Track saves commit a check-in then its joins separately (`S/ui/MainViewModel.kt:1840–1868`, `data/Repository.kt:1346,1914`). Review completion separately inserts a Record, inserts a Review, then launches `addTrackEntry` (`MainViewModel.kt:2676–2705`). | Move the whole intended Track/Review application operation into one transactional boundary. A Room trigger rejecting a Track join left the check-in committed: **reproduced**. Review failure/cancellation is source verified, not independently fault-injected. Test failure between every constituent write and await completion. |
| **F05 — retries/pins/recurring successors** | **Reported repository bugs resolved; stronger identity contract deferred.** Current lifecycle/triage/pin commands reload state, serialize writers, and deduplicate. Non-unique pin indexes still permit direct DAO duplicates; generic operation/occurrence IDs are absent. | Preserve current tests. Add SQL uniqueness/operation identity only when the approved contract requires it. This is not a claim that normal repository retries currently duplicate successors. SP03/SP04 remain closed. |
| **F06 — schedule ownership** | **Partial, P1 architecture/date gate.** Unrelated edits preserve rich rows; ambiguous mirrors reject; recurrence-only edits preserve unresolved timezone metadata (`LegacyScheduleReconciliation.kt`). Scalars and rich schedule rows still share authority; recurrence in `RecurrenceDates.kt` adds fixed 1/7/30-day durations. | Define mirror ownership and calendar/date-only/DST behavior before calling recurrence calendar-month safe. Preserve rollback and schedule-identity tests. SP04/SP05 remain closed. |
| **F07 — effective Area membership** | **Open decision/consistency issue, P1.** `S/data/Daos.kt:49–88` queries direct `projectId`/`areaId`; Project Area does not automatically become its Task's Area. | Choose inheritance and explicit override rules. Verify one Task across Project/Area/core views without clones, including moving its Project. Direct-only membership may be intentional but must be consistent and legible. |
| **F08 — pointers/relations/deletion** | **Open, P1; strengthened by reproduction.** `Repository.kt:943–966` removes all outgoing `BELONGS_TO` edges then rebuilds Project/Area edges. `deleteNode:1209–1225` removes edges but does not clear surviving nodes' `projectId`/`areaId`/`parentNodeId`. A Room probe deleting a Project left its child Task pointing to the missing Project while its relation disappeared. | Define authority and deletion policy (detach/reassign/restrict) for each role; preserve unrelated allowed edges and validate endpoints. Test Project, Area, and parent deletion with children. Pointer/edge drift is more than duplicate storage overhead. |
| **F09 — domains versus heuristic lenses** | **Open decision/consistency issue, P1.** `ItemDomainEntity` assignments coexist with `DomainLensQueries.kt:103–125`, which explicitly bypasses them; Notes adds a separate inferred-domain projection. | Define how explicit membership, heuristics, tags, and Area interact. Test explicit Education assignment with a health keyword. Heuristic suggestions can remain useful; avoid silently treating all projections as one authority. |
| **F10 — portable identity/deletion** | **Deferred portability/sync gate, P3.** Room-generated local Long IDs and physical deletion remain. | Do not present same-store restore as unrelated-store merge. Define stable identity, reference remapping, revisions, and tombstones if that capability is approved; local integer keys need not all be replaced. |
| **F11 — legacy spine/typed facet authority** | **Open architecture debt, P1.** `Entities.kt`, `LifeObjectEntities.kt`, `LifeObjectMappings.kt`, and `Repository.kt:971+` retain writable node values plus mirrored typed companions/documents/domains. Nullable task clear is repaired. | Define field writers/readers and compatibility transitions one concern at a time. Test set/change/clear and unknown-type/metadata handling. The typed tables themselves are not redundant simply because migration is incomplete. |
| **F12 — saved views lack UI execution** | **Open latent-feature/documentation gap, P2.** `Repository.kt:1521–1637` persists definitions; current app sources have no presentation call to `getSavedViews`, `getSavedView`, or `saveSavedView`. | Explicitly describe this as persistence groundwork, or deliver save/restart/open/live-query behavior when prioritized. Do not delete the schema as “dead code” without considering stored data and migration policy. |
| **F13 — capture versus direct create** | **Open workflow decision/acceptance gate, P2.** Raw `InboxEntry` triage and direct structured creation coexist. Triage retry safety is repaired. | Clarify pending raw capture versus a structured item; prove later assignment/provenance and one canonical ID. Their coexistence alone is not redundant functionality or a reason to remove Android share. |
| **F14 — legacy types/core ontology** | **Open compatibility/design decision, P2.** `LifeObjectMappings.kt` retains aliases/specialist types alongside Task/Note/Record/Project/Area. | Document alias/facet ownership and test old IDs/routes. Preserve valuable specialist flows; route count alone does not prove feature duplication. |
| **F15 — Dashboard density** | **Usability hypothesis, P2; no demonstrated task failure.** `C/ui/screens/dashboard/DashboardLayoutEngine.kt` still has large default block lists. | Test fresh narrow/wide layouts and primary tasks before reducing defaults. Preserve customization and distinguish Dashboard relevance from explicit Today pins. |
| **F16 — synthetic Finance values/status** | **Open source-confirmed defect, P2.** `FinancesScreen.kt:85–94` sums `financeSyntheticLiquidity(title)`; `FinanceDashboardBlocks.kt:486–494` derives a value from title characters. Currency and static LIVE/SYNC controls imply unsupported facts. | Remove synthetic money/status or back them with typed persisted amounts/currency/provenance. Renaming a title must not change an actual money total. |
| **F17 — Study/global identity** | **Acceptance gate, P2.** Shared student commands and assignment/reading/session workflows remain substantive. No new duplicate Study store was established. | Prove global Task/Note identity, completion/date propagation, and one session lineage. Retain the workflow rather than deleting it to simplify navigation. |
| **F18 — Track/Health state and daily save semantics** | **Open, P1 for saved-state behavior.** `TrackScreen.kt:77` starts medication selection empty; stored joins are not collected there. It reloads daily scalar values but Save passes a fresh `TrackEntryEntity(id = 0)` (`MainViewModel.kt:1825–1868`). Same-day date is not unique (`Entities.kt:483–488`). Two identical-date insert shapes produced different IDs: **reproduced**. | Hydrate medication state; decide explicitly whether Save edits the daily entry or appends a check-in. If appending is intended, show and aggregate that history consistently instead of presenting an implicit daily edit. Test reopen/date change/repeated Save; coordinate aggregate atomicity with F04. The current hardcoded mobile plan is tracked under F32. |
| **F19 — active dead controls/accessibility** | **Open source-confirmed examples, P2; runtime accessibility still unverified.** `AreaDetailBlocks.kt:158–160` exposes Work/Notes/Projects buttons with empty handlers; Finance has similar active controls. `notes/detail/NoteDetailScreen.kt:214–259` has icon-only pin/copy/delete buttons with null descriptions and no action label on the enclosing button in that code. | Implement, disable, or omit misleading actions; supply localized accessible names where parent semantics do not. Do not count every informational AssistChip as a missing feature. Verify keyboard, focus, screen-reader, large-text, and touch behavior on actual targets. |
| **F20 — ephemeral server store** | **Deferred prototype limitation, P3.** `server/.../routes/SyncRoutes.kt:21` uses process-global `LinkedHashMap`; no app sync caller was established. | If production sync is approved, add durable owner-scoped storage and prove restart/multi-instance/offline behavior. Do not claim durability from passing server request tests. |
| **F21 — timestamp sync protocol** | **Deferred prototype limitation, P3.** Same route accepts `updatedAt >= existing` and exports `updatedAt > lastSyncTime`; DTO version/client/delete fields do not establish complete semantics. | Before production use, define ordered checkpoints, equal-clock conflicts, revisions, tombstones, pagination, and retry behavior. Keep cancellation handling that is already tested. |
| **F22 — server principal model** | **Deferred hosted-service gate, P3.** `server/.../Application.kt:33–64` authenticates one configured token as `sync-client`. | Suitable scope must be stated; hosted multi-user exposure requires owner isolation and separate acceptance. A single-owner prototype is not automatically a broken account system. |
| **F23 — target/release/documentation drift** | **Open source-confirmed workflow problem plus release gates, P2.** `website/` is absent, but `.github/workflows/astro.yml` still expects it and manual package detection reaches an error. README/AGENTS/canonical docs still describe website setup/structure. Android versions/signing and iOS readiness were not freshly artifact-validated. | Reconcile obsolete website workflow/docs without incidentally publishing/removing a deployed site. Verify actual release signatures/versions and Desktop install/upgrade before release claims; iOS remains a scaffold. |
| **F24 — historical website claims** | **Checked-in source manifestation closed; publication unverified.** `b704903e` removed the site pages containing unsupported service/security/pricing claims. | No active source-copy finding remains to fix. Live Pages content/configuration was not inspected; if the site returns, substantiate its claims. |
| **F25 — wider integrations** | **Proposal/deferred, P3.** Calendar ICS and Android share are narrow adapters; no general project connector was established. | Preserve provenance and canonical item identity if references/connectors are later approved. Absence of an unpromised integration is not a defect. |
| **F26 — relationship date collision** | **Open source-confirmed defect, P1.** `S/ui/main/actions/RelationshipCommands.kt:57–83` writes both follow-up and important-date values to `person.dueAt`. | Give independent date roles appropriate storage; test both save orders and preservation of the other value. Choose the product model before introducing another derived Task. |

## Existing repairs and exclusions

The current full test pass preserves the earlier fixes: **SP01/F01–F02–F04** provider credential identity/redaction; **SP02/F02** explicit backup version; **SP03/F04–F05** stale-target pin protection/all-date deletion; **SP04/F04–F05–F06** repeated batch-ID last-row side effects; **SP05/F06** recurrence-only unknown-timezone preservation. The relevant adversarial, persistence, import, safety, guardrail, and Settings-transfer suites are included in the shared/Compose baseline. F04's new gaps concern different application-level operations, not a reversal of those repairs.

Also retain schedule ambiguity rejection/rollback, nullable facet clearing, deterministic analytical inputs, cancellation propagation, and shared `TaskRow` interactions. The review found no basis to re-open those repaired mechanisms.

The following were deliberately excluded from duplicate-defect counts:

- `expect`/`actual` platform adapters and generated Room code; same names express required platform contracts.
- MainViewModel → command → repository → DAO delegation and type-specific `toModel`/`fromStorageKey` overloads. Matching names do not mean matching responsibility.
- Legitimate specialized views over the same IDs, including Study and Record's delegation to note detail.
- Material/Compose container repetition without a stable shared behavior to extract.
- Previews with no production caller; declaration-only searches alone cannot mark a preview dead.
- Finance's richer layout overrides and feature-specific contexts; F32 does not justify one universal dashboard implementation.
- The old blanket Places linkage concern: `PlacesScreen.kt:385–386` now passes a place ID to `createWhatToBringList`, which forwards it through `RelationshipCommands`. No blanket claim that this path drops the place ID is carried forward. Other draft/template semantics still need a focused product test before being called broken.

## Recommended cleanup order

1. **Protect persisted intent:** F27 note patches/drafts, F04 complete Track/Review operations, F08 deletion consistency, F18 medication reload/daily-save contract, F26 independent dates, F35 Protocol notes. Decide F03's durability promise before inviting irreplaceable data.
2. **Repair time invalidation:** F28 with an injected date/timezone signal and a same-subscription rollover test.
3. **Remove demonstrated duplication:** F29 obsolete Notes islands, F30 one medication editor/validator, F31 one Parcelable implementation. Review independent changes in small coherent commits.
4. **Reduce repeated presentation machinery:** F32 equivalent branches, F33 localization, F34 targeted ownership/wiring. Preserve canonical IDs, rich layouts, and current command contracts.
5. **Resolve truth/documentation gaps:** F16, F19, F23; settle F07/F09/F11 field/domain ownership before expanding projections. Keep conditional sync/integration work deferred unless separately requested.

These are recommendations, not authorization to implement every product/data-policy change. This request changes audit documentation only.

## Validation and reproducibility

**Current baseline: 626 tests / 80 suites, zero failures, errors, or skips.** Shared: 513 / 65 suites; Compose: 104 / 13; server: 9 / 2. Test tasks were explicitly executed with task-level `--rerun`, while compilation/resource outputs reused incremental caches. Original command:

```powershell
.\gradlew.bat :shared:jvmTest --rerun :composeApp:jvmTest --rerun :server:test --rerun --console=plain
```

After the isolated audit probes, the normal shared suite was executed again without adding the probe source directory, together with Android lint:

```powershell
.\gradlew.bat :shared:jvmTest --rerun :androidApp:lintDebug --console=plain
```

Both runs returned **BUILD SUCCESSFUL**. The latter reused some Android lint/build outputs; this is validation of current inputs with Gradle's cache, not a claim that all Android analysis was recomputed. Lint XML reports **0 errors / 8 warnings**: one DataExtractionRules compatibility warning, one unnecessary drawable-v24 qualifier, four unused launcher-resource groups, and two TypographyEllipsis warnings. `android:allowBackup="false"` is already set; the compatibility warning alone does not prove private data is exposed. The unused old launcher assets are bounded P3 cleanup candidates, separate from the active logo resources. Existing JDK native-access/Unsafe and Gradle deprecation warnings also remain. No detekt/ktlint configuration was found in the inspected build/workflow configuration; this audit does not imply those tools ran.

**Four additional real-Room characterization probes passed**, deliberately asserting the current problematic behavior: stale disjoint note edit overwrites the earlier title; same-date Track save shapes create separate IDs; injected Track join failure leaves a committed check-in; deleted Project leaves a dangling child pointer. These are evidence of defects/contract mismatches, not passing regression tests for correct behavior, and are excluded from the 626 baseline total. No real profile or device data was used.

The probes live locally at `.codex/temp/quality-audit-probes/QualityAuditProbeTest.kt`; an init script adds that directory only for the isolated invocation:

```powershell
.\gradlew.bat -I .codex/temp/quality-audit-probes.init.gradle --no-configuration-cache :shared:jvmTest --tests com.tajemniktv.tajos.data.QualityAuditProbeTest --rerun --console=plain
```

Local evidence: `.codex/temp/quality-audit-{jvm-tests,shared-lint,probes}.log`, `quality-audit-probes.xml`, `quality-audit-baseline-results/`, and `quality-audit-test-summary.json`. The read-only candidate scanner and raw results are `quality-audit-scan.py` / `quality-audit-clones.json`. Its 12-line normalized exact-window scan is a **candidate finder**, not a semantic duplicate percentage: it found 22 matching windows in five file-pair groups; manual call-path review found additional semantic duplication and excluded parameter/delegation boilerplate. Temporary evidence files are not application source or required build inputs.

No production source, schema, policy, dependency, or normal test source changed. No Android device/physical-key, Desktop visual/typing/accessibility/install, iOS, live server/deployment, full translation, upgrade-loss, private-data recovery, or dependency-advisory validation was performed. The Today rollover and Protocol UI paths remain source verified rather than dynamically reproduced. Passing baseline tests does not close these acceptance gaps.

Pre-existing worktree deletions of `.tmp_closed_prs.json`, `gitapplypatchbuild.txt`, and `renovate.json` were preserved. The source snapshot remained the same throughout the audit; its documentation commit is recorded in Git history rather than changing this reviewed-source reference.
