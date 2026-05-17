import re

file_path = "shared/src/commonMain/kotlin/com/tajemniktv/tajsos/data/Entities.kt"
with open(file_path, "r") as f:
    content = f.read()

replacement3 = """/**
 * ModePreferenceEntity stores the user-customizable configuration for a specific operating mode.
 *
 * @property id The unique primary key.
 * @property modeId The ID of the ModeEntity this preference is linked to.
 * @property showInbox Whether the inbox section is visible in this mode.
 * @property showStats Whether the statistics section is visible in this mode.
 * @property showNotes Whether the notes section is visible in this mode.
 * @property showResources Whether the resources section is visible in this mode.
 * @property showDeadlines Whether the deadlines section is visible in this mode.
 * @property showOpenLoops Whether the open loops section is visible in this mode.
 * @property maxVisibleTasks The maximum number of tasks to display in a list.
 * @property sortStrategy The identifier indicating how the resulting nodes should be sorted (e.g., "DEFAULT", "URGENCY").
 * @property quickActionsJson A JSON string defining the available quick actions.
 * @property defaultQuickActionsJson A JSON string defining the default quick actions for this mode.
 * @property dashboardBlocksJson A JSON string defining the modular dashboard components to render.
 * @property filterProfileJson A JSON string storing advanced filter configurations for this mode.
 * @property suggestionProfileJson A JSON string defining the AI or context-based suggestions relevant to this mode.
 */
@Entity(
    tableName = "mode_preferences",
    indices = [Index(value = ["modeId"], unique = true)],
)
@Serializable
data class ModePreferenceEntity"""

content = content.replace("@Entity(\n    tableName = \"mode_preferences\",\n    indices = [Index(value = [\"modeId\"], unique = true)],\n)\n@Serializable\ndata class ModePreferenceEntity", replacement3)

with open(file_path, "w") as f:
    f.write(content)
