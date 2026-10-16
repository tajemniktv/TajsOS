# Audit decisions

16 October 2026. These choices remain separate from the correctness/cleanup fixes. Audit IDs are stable; decision numbers are shortcuts for replying. Recommendations below have **not** been implemented unless noted in `AUDIT.md`.

## Decisions that affect data

### D01 — When does a profile become durable? (F03)

- **Issue:** an unsupported schema can erase local data; an empty existing profile receives example nodes again.
- **Options:** keep a clearly disposable development profile; or protect profiles with explicit migrations and block unsupported upgrades. Either approach can restrict samples to explicit first-time setup.
- **Implications:** directly, convenience versus data preservation. Indirectly, durable profiles require an upgrade test matrix, recovery UX, and a supported-version policy; merely removing fallback without recovery can prevent launch.
- **Recommendation:** protect existing profiles and make example seeding explicit. Keep destructive resets available only as a deliberate development action. Establish the supported upgrade range before changing all builders.

### D02 — What does a backup promise, and may it contain private URLs? (F01, F02)

- **Issue:** current JSON recovers Room data, but excludes attachment bytes, URI grants, and device settings. Private feed URLs can grant access even after token redaction.
- **Options:** accurately scoped private database recovery; a broader encrypted portable archive; or separate private recovery and sanitized sharing exports.
- **Implications:** directly, restore completeness and credential exposure. Indirectly, portable files need a manifest, attachment storage policy, platform reauthorization, and format/version handling. Encryption adds key recovery and forgotten-password consequences. Cross-dataset merge additionally requires identity mapping.
- **Recommendation:** explicitly label today's format as private database recovery, then offer sanitized sharing separately. Design a versioned archive/manifest before adding external files; do not imply cross-store merge. Choose encryption/key recovery together rather than adding an unexplained password field.

### D03 — What happens when a Project, Area, or parent is deleted? (F08)

- **Issue:** children can retain pointers to a deleted object; pointer synchronization can also remove unrelated `BELONGS_TO` roles.
- **Options:** detach children; refuse deletion until reassignment; or cascade-delete the subtree. Relation roles can be derived from pointers or independently editable, but need distinct ownership.
- **Implications:** directly, where surviving items appear and whether any child data is deleted. Indirectly, search/counts, undo, graph roles, and existing orphan repair must follow the same policy.
- **Recommendation:** preserve children and detach only the deleted membership; offer reassignment before deletion. Never silently cascade-delete user content. Let Project/Area pointers own their matching edges, with separate roles for independent graph relationships. Historical dangling pointers need a cautious repair pass.

### D04 — Does a Task inherit its Project's Area? (F07)

- **Issue:** direct-only queries omit a Project-only Task from its Project's Area.
- **Options:** independent assignments; inherited Area with an explicit Task override; or Project-controlled Area with no override.
- **Implications:** directly, Area membership and counts. Indirectly, moving a Project may move many Tasks between views; copying inherited values into every Task creates synchronization work and obscures overrides.
- **Recommendation:** explicit Task Area overrides; otherwise inherit Project Area in a shared read model. Avoid copying inherited values into Task rows. Use the same effective membership in lists, filters, and totals.

### D05 — Do explicit domains or heuristics control lenses? (F09)

- **Issue:** persisted domain assignments, Finance/Health keyword matching, and Notes domain inference disagree.
- **Options:** heuristics only; union of explicit and inferred domains; or explicit assignments with heuristic suggestions/fallback when unclassified.
- **Implications:** directly, predictable inclusion versus automatic discovery. Indirectly, changing keywords could otherwise move items unexpectedly; existing metadata/domain mirrors need reconciliation, and multi-domain items need an explicit rule.
- **Recommendation:** explicit assignments control membership; heuristics suggest or supply a visibly inferred fallback for unclassified items. Allow multiple explicit domains and keep Project/Area/tag concepts separate.

### D06 — What does recurring mean across months and DST? (F06)

- **Issue:** DAILY/WEEKLY/MONTHLY currently add fixed durations, including 30 days for a month.
- **Options:** preserve elapsed-duration intervals; use calendar-local recurrence; or expose the two concepts separately.
- **Implications:** directly, actual due dates, month-end behavior, and DST wall-clock times. Indirectly, old schedules/backups need compatibility rules, explicit timezone/date-only semantics, and tests for ambiguous/nonexistent times.
- **Recommendation:** named daily/weekly/monthly recurrence should follow the item's local calendar; month-end clamps to the last valid day. Keep elapsed-duration recurrence as a distinct concept if needed. Version or explicitly migrate existing intervals rather than silently reinterpreting them.

### D07 — Is Track saving a daily entry or a new check-in? (F18)

- **Issue:** the form reloads today's latest values, but each Save appends a row. Reviews can append sparse mood/energy check-ins too. Medication joins are now hydrated, and each save is atomic; the daily semantics remain unchanged.
- **Options:** one editable entry per day; timestamped check-ins; or explicit New check-in/Edit selected actions with a defined daily summary.
- **Implications:** directly, history size and whether a correction creates another measurement. Indirectly, averages/trends and medication completion need an aggregation rule; a sparse Review check-in must not implicitly erase the day's medication state.
- **Recommendation:** preserve timestamped history, distinguish New check-in from Edit selected, and define a separate daily summary. This retains existing records and supports changes during a day. Do not deduplicate historical same-date rows automatically.

### D08 — Where should Protocol session notes live? (F35)

- **Issue:** End session currently clears entered notes without persisting them; trigger history does not identify the editor's run explicitly.
- **Options:** store notes on the existing `ProtocolHistoryEntity` for an explicit run ID; create one linked canonical Record; or deliberately make the input temporary and label it accordingly.
- **Implications:** directly, recovery and where users find the text. Indirectly, restart/resume, switching runs, global search, and avoiding duplicate histories need a stable run association. Saving to “the latest row” is unsafe.
- **Recommendation:** bind the run to its existing history ID and store notes there first, with save/retry before clearing. Add a linked Record only if global knowledge/search is an intended feature. This remains an open P1 issue; no guessed owner or second history store was introduced.

### D09 — How should a person's two dates be modeled? (F26)

- **Issue:** follow-up and important date both overwrite `person.dueAt`; old values cannot reveal which role they meant.
- **Options:** separate Person fields/facet; independent typed schedule roles; or a linked follow-up Task plus an important-date schedule.
- **Implications:** directly, preserving both dates. Indirectly, birthdays may recur while follow-ups should appear in actionable Tasks; counts/reminders and legacy migration must avoid duplicate events or invented date meaning.
- **Recommendation:** a linked Task for actionable follow-up, with a separate typed Person schedule for the important date. Retain an ambiguous legacy date until its role is chosen. If follow-ups are intentionally only CRM metadata, choose two schedule roles instead.

### D10 — Which representation owns each field during the typed-model transition? (F11, F14)

- **Issue:** the legacy node spine and typed facets/documents/domains remain writable mirrors, while specialist aliases still serve real routes.
- **Options:** node-authoritative with derived companions; typed-authoritative with a compatibility projection; or an immediate breaking migration.
- **Implications:** directly, which update wins. Indirectly, import/export compatibility, unknown metadata, selectors, and old deep links must remain consistent; a large immediate migration multiplies regression risk.
- **Recommendation:** publish a field-owner matrix, then make typed state authoritative one concern at a time with explicit compatibility mapping. Preserve specialist workflows and IDs. The current text/metadata patch fixes do not settle this broader authority question.

## Product priorities and conditional work

| Choice | Options and implications | Recommendation |
| --- | --- | --- |
| **Saved views (F12)** | Keep tested persistence groundwork explicitly latent, or build save/reopen/live-query UI. Shipping it means defining one shared query/filter contract; deleting schema would risk stored data. | Keep it latent until membership/field authority is settled; then deliver one useful saved-query slice. |
| **Capture workflow (F13)** | Raw inbox-first, direct typed creation, or explicit support for both. Forced triage adds friction; unrestricted direct creation needs clear queue state/provenance. | Support both with clear pending-capture versus structured-item semantics; prove one capture → assign → find → complete path before redesign. |
| **Dashboard defaults (F15)** | Retain customizable density or trial a calmer default. This changes information priority and discovery; source block counts are not usability evidence. | Test a calmer default with capture, next action, and next commitment, while retaining customization. Do not remove blocks based only on count. |
| **Release/support promises (remaining F23)** | Continue clearly labeled developer previews or define signed/versioned release channels. A real release promise brings upgrade, signature, install, and supported-platform obligations. | Keep Android/Desktop as primary; treat iOS as scaffold. Decide release/signing/version policy together. Obsolete website workflows/docs were cleaned up without changing live Pages state. |
| **Portable identity, production sync, wider connectors (F10, F20–F22, F25)** | Defer, self-host for one owner, or design a hosted service. Production use needs stable IDs, durability, conflict/tombstone semantics, and principal isolation, not just a transport. | Defer while the local model is being stabilized. No current connector or production-service commitment is assumed. |

F17's cross-view Study proof and F19's platform/accessibility acceptance are validation work, not product decisions. F34's remaining feature-state extraction is routine incremental architecture work; it does not require a blanket approval for a rewrite.
