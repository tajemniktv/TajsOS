import re

with open("shared/src/commonTest/kotlin/com/tajemniktv/tajsos/ui/FilterHelperMissingCoverageTest.kt", "r") as f:
    content = f.read()

replacement = """
    private data class FilterConfig(
        val nodes: List<NodeWithPin>,
        val projectId: Long? = null,
        val areaId: Long? = null,
        val linkedToId: Long? = null,
        val maxMins: Int? = null,
        val energy: Int? = null,
        val locationContext: String? = null,
        val energyContext: String? = null,
        val deviceContext: String? = null,
        val socialContext: String? = null,
        val timeWindowContext: String? = null,
        val timeHorizon: String? = null,
        val relations: List<RelationEntity> = emptyList()
    )

    private fun filter(config: FilterConfig): List<NodeWithPin> {
        return FilterHelper.filterAndSortNodes(
            nodes = config.nodes, query = "", type = null, status = null, projectId = config.projectId, areaId = config.areaId,
            linkedToId = config.linkedToId, maxMins = config.maxMins, energy = config.energy, friction = null,
            locationContext = config.locationContext, energyContext = config.energyContext, deviceContext = config.deviceContext,
            socialContext = config.socialContext, timeWindowContext = config.timeWindowContext, timeHorizon = config.timeHorizon,
            relations = config.relations, sortMode = "relevance"
        )
    }
"""

content = re.sub(r'private fun filter\(.*?\{.*?\}', replacement, content, flags=re.DOTALL)

# Because all of them are on one line and end with ), we can do regex replace
content = re.sub(r'filter\((nodes = listOf\([^)]*\)[^)]*)\)', r'filter(FilterConfig(\1))', content)

with open("shared/src/commonTest/kotlin/com/tajemniktv/tajsos/ui/FilterHelperMissingCoverageTest.kt", "w") as f:
    f.write(content)
