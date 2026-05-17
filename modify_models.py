import re

file_path = "shared/src/commonMain/kotlin/com/tajemniktv/tajsos/data/LifeObjectModels.kt"
with open(file_path, "r") as f:
    content = f.read()

replacement = """/**
 * Typed task facet returned by repository read models.
 *
 * @property state The execution state of the task (e.g., ACTIVE, DONE).
 * @property energyLevel An optional integer representing the energy required to complete the task.
 * @property friction An optional text describing any resistance or blockers.
 * @property nextStep An optional concrete next action to move the task forward.
 * @property estimatedMinutes An optional estimated duration for the task in minutes.
 * @property completionNote An optional note added when the task is completed.
 * @property completedAt An optional timestamp (epoch milliseconds) of when the task was completed.
 * @property isRecurring Indicates if the task repeats.
 * @property recurringInterval The recurrence rule/interval string if the task is recurring.
 */
@Serializable
data class TaskFacet"""

content = content.replace("/**\n * Typed task facet returned by repository read models.\n */\n@Serializable\ndata class TaskFacet", replacement)

replacement_note = """/**
 * Typed note facet returned by repository read models.
 *
 * @property kind The category or type of the note (e.g., GENERAL, MEETING).
 * @property state The lifecycle state of the note.
 * @property sourceTitle An optional title of the source material this note references.
 * @property sourceAuthor An optional author of the source material.
 * @property lastReviewedAt An optional timestamp (epoch milliseconds) of when the note was last reviewed.
 */
@Serializable
data class NoteFacet"""

content = content.replace("/**\n * Typed note facet returned by repository read models.\n */\n@Serializable\ndata class NoteFacet", replacement_note)

replacement_project = """/**
 * Typed project facet returned by repository read models.
 *
 * @property state The execution state of the project (e.g., ACTIVE, COMPLETED).
 * @property purpose An optional description of the project's goal or desired outcome.
 * @property isFrozen Indicates if the project is currently suspended or on hold.
 */
@Serializable
data class ProjectFacet"""

content = content.replace("/**\n * Typed project facet returned by repository read models.\n */\n@Serializable\ndata class ProjectFacet", replacement_project)

replacement_record = """/**
 * Typed record facet returned by repository read models.
 *
 * @property kind The specific category of the record (e.g., GENERAL, HEALTH).
 * @property occurredAt The timestamp (epoch milliseconds) when the recorded event occurred.
 */
@Serializable
data class RecordFacet"""

content = content.replace("/**\n * Typed record facet returned by repository read models.\n */\n@Serializable\ndata class RecordFacet", replacement_record)

replacement_area = """/**
 * Typed area facet returned by repository read models.
 *
 * @property healthStatus The current status/health of the area (e.g., STABLE, ATTENTION_NEEDED).
 * @property standardOfCare An optional description defining what "good" looks like for this area.
 * @property vision An optional long-term vision or desired state for this area.
 */
@Serializable
data class AreaFacet"""

content = content.replace("/**\n * Typed area facet returned by repository read models.\n */\n@Serializable\ndata class AreaFacet", replacement_area)

with open(file_path, "w") as f:
    f.write(content)
