import re

file_path = "shared/src/commonMain/kotlin/com/tajemniktv/tajsos/data/Entities.kt"
with open(file_path, "r") as f:
    content = f.read()

replacement = """/**
 * ModeEntity defines an Operating Mode profile.
 *
 * @property id The unique primary key of the mode.
 * @property key The unique identifier for the mode (e.g., COMMAND, FOCUS, RECOVERY).
 * @property name The human-readable name of the mode.
 * @property description An optional explanation of the mode's purpose.
 * @property icon An optional string identifier for the mode's icon.
 * @property themeColor An optional integer representing the theme color for this mode.
 * @property isBuiltin Whether this mode is a built-in system default or user-created.
 * @property isEnabled Whether this mode is currently active and selectable.
 * @property sortOrder The display order of the mode in lists.
 * @property createdAt Timestamp when the mode was created.
 * @property updatedAt Timestamp when the mode was last updated.
 */
@Entity(tableName = "modes")
@Serializable
@Immutable
data class ModeEntity"""

content = content.replace("/**\n * ModeEntity defines an Operating Mode profile.\n */\n@Entity(tableName = \"modes\")\n@Serializable\n@Immutable\ndata class ModeEntity", replacement)

replacement2 = """/**
 * TrackEntryEntity handles daily micro check-ins.
 *
 * @property id The unique primary key of the track entry.
 * @property date The date of the track entry in YYYY-MM-DD format.
 * @property createdAt The timestamp (epoch milliseconds) when the entry was created.
 * @property moodScore An optional score representing the user's mood.
 * @property energyScore An optional score representing the user's energy level.
 * @property focusScore An optional score representing the user's focus level.
 * @property anxietyScore An optional score representing the user's anxiety level.
 * @property sleepScore An optional score representing the user's sleep quality.
 * @property tookMeds Indicates if the user took their tracked medications.
 * @property symptomNote A text note for any observed symptoms or health details.
 * @property source The origin of the entry (e.g., manual, inferred, reminder).
 * @property loadScore LifeOS status tracking representing overall cognitive or physical load (0-100).
 * @property fragmentationScore LifeOS status tracking representing task or context fragmentation (0-100).
 */
@Entity(tableName = "track_entries")
@Serializable
data class TrackEntryEntity"""

content = content.replace("/**\n * TrackEntryEntity handles daily micro check-ins.\n */\n@Entity(tableName = \"track_entries\")\n@Serializable\ndata class TrackEntryEntity", replacement2)

with open(file_path, "w") as f:
    f.write(content)
