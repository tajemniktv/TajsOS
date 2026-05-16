import os

file_path = "shared/src/commonTest/kotlin/com/tajemniktv/tajsos/ui/main/actions/ProtocolCommandsTest.kt"
with open(file_path, "r") as f:
    content = f.read()

# Make the createCommands helper accept protocolTemplates and playbookTemplates, with default empty lists, or we can just replace the other duplicates.
# Let's see if we can use createCommands in testTriggerProtocolCreatesNewNode, testApplyProtocolTemplateCreatesNewNode, testApplyPlaybookTemplateCreatesNodeAndTags

helper_new = """
    private fun createCommands(
        repo: AppRepository,
        scope: TestScope,
        protocolTemplates: List<TransitionProtocolTemplate> = emptyList(),
        playbookTemplates: List<PlaybookTemplate> = emptyList()
    ): ProtocolCommands {
        return ProtocolCommands(
            repository = repo,
            scope = scope,
            currentNodes = { emptyList() },
            currentTags = { emptyList() },
            protocolTemplates = { protocolTemplates },
            playbookTemplates = { playbookTemplates }
        )
    }
"""

content = content.replace("""    private fun createCommands(repo: AppRepository, scope: TestScope): ProtocolCommands {
        return ProtocolCommands(
            repository = repo,
            scope = scope,
            currentNodes = { emptyList() },
            currentTags = { emptyList() },
            protocolTemplates = { emptyList() },
            playbookTemplates = { emptyList() }
        )
    }""", helper_new)


# Now replace the duplicates in the first 3 tests

content = content.replace("""        var currentNodes = emptyList<NodeWithPin>()

        val commands = ProtocolCommands(
            repository = repo,
            scope = scope,
            currentNodes = { currentNodes },
            currentTags = { emptyList() },
            protocolTemplates = { emptyList() },
            playbookTemplates = { emptyList() }
        )""", """        var currentNodes = emptyList<NodeWithPin>()

        val commands = ProtocolCommands(
            repository = repo,
            scope = scope,
            currentNodes = { currentNodes },
            currentTags = { emptyList() },
            protocolTemplates = { emptyList() },
            playbookTemplates = { emptyList() }
        )""") # wait this one uses currentNodes, so we'll leave it or modify the helper. Let's just leave it or pass currentNodes.

# Let's modify the helper to accept currentNodes.

helper_v3 = """
    private fun createCommands(
        repo: AppRepository,
        scope: TestScope,
        currentNodes: List<NodeWithPin> = emptyList(),
        protocolTemplates: List<TransitionProtocolTemplate> = emptyList(),
        playbookTemplates: List<PlaybookTemplate> = emptyList()
    ): ProtocolCommands {
        return ProtocolCommands(
            repository = repo,
            scope = scope,
            currentNodes = { currentNodes },
            currentTags = { emptyList() },
            protocolTemplates = { protocolTemplates },
            playbookTemplates = { playbookTemplates }
        )
    }
"""

content = content.replace(helper_new, helper_v3)

# Replace testTriggerProtocolCreatesNewNode duplicate
content = content.replace("""        var currentNodes = emptyList<NodeWithPin>()

        val commands = ProtocolCommands(
            repository = repo,
            scope = scope,
            currentNodes = { currentNodes },
            currentTags = { emptyList() },
            protocolTemplates = { emptyList() },
            playbookTemplates = { emptyList() }
        )""", """        val commands = createCommands(repo, scope)""")

# Replace testApplyProtocolTemplateCreatesNewNode
content = content.replace("""        val commands = ProtocolCommands(
            repository = repo,
            scope = scope,
            currentNodes = { emptyList() },
            currentTags = { emptyList() },
            protocolTemplates = { listOf(template) },
            playbookTemplates = { emptyList() }
        )""", """        val commands = createCommands(repo, scope, protocolTemplates = listOf(template))""")

# Replace testApplyPlaybookTemplateCreatesNodeAndTags
content = content.replace("""        val commands = ProtocolCommands(
            repository = repo,
            scope = scope,
            currentNodes = { emptyList() },
            currentTags = { emptyList() },
            protocolTemplates = { emptyList() },
            playbookTemplates = { listOf(template) }
        )""", """        val commands = createCommands(repo, scope, playbookTemplates = listOf(template))""")


with open(file_path, "w") as f:
    f.write(content)
print("Removed more duplicates.")
