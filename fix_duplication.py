import os

file_path = "shared/src/commonTest/kotlin/com/tajemniktv/tajsos/ui/main/actions/ProtocolCommandsTest.kt"
with open(file_path, "r") as f:
    content = f.read()

# Introduce a helper method for setting up tests to reduce duplication of ProtocolCommands creation
helper = """
    private fun createCommands(repo: AppRepository, scope: TestScope): ProtocolCommands {
        return ProtocolCommands(
            repository = repo,
            scope = scope,
            currentNodes = { emptyList() },
            currentTags = { emptyList() },
            protocolTemplates = { emptyList() },
            playbookTemplates = { emptyList() }
        )
    }
"""

# Insert the helper
insertion_idx = content.find("    @Test")
content = content[:insertion_idx] + helper + "\n" + content[insertion_idx:]

# Replace duplicates
duplicate_block = """        val commands = ProtocolCommands(
            repository = repo,
            scope = scope,
            currentNodes = { emptyList() },
            currentTags = { emptyList() },
            protocolTemplates = { emptyList() },
            playbookTemplates = { emptyList() }
        )"""

content = content.replace(duplicate_block, "        val commands = createCommands(repo, scope)")

with open(file_path, "w") as f:
    f.write(content)
print("Removed duplicates.")
