package com.tajemniktv.tajsos.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tajemniktv.tajsos.ui.MainViewModel
import com.tajemniktv.tajsos.ui.components.NodeCard
import com.tajemniktv.tajsos.ui.theme.TactileTheme

@Composable
fun AreaDetailScreen(
    viewModel: MainViewModel, 
    areaId: Long, 
    onNavigateToProject: (Long) -> Unit,
    onEditNode: (Long) -> Unit
) {
    val areas by viewModel.allAreas.collectAsState()
    val allNodes by viewModel.allNodes.collectAsState()
    val area = areas.find { it.id == areaId }
    val projects by viewModel.getProjectsForArea(areaId).collectAsState(initial = emptyList())
    val nodesWithPin by viewModel.getNodesForArea(areaId).collectAsState(initial = emptyList())

    if (area == null) {
        Box(modifier = Modifier.fillMaxSize()) {
            Text("Area not found", modifier = Modifier.padding(TactileTheme.SpacingMd))
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(TactileTheme.SpacingMd)
    ) {
        Text(
            text = area.title.uppercase(),
            style = MaterialTheme.typography.displaySmall,
            color = TactileTheme.Text
        )
        Spacer(modifier = Modifier.height(TactileTheme.SpacingMd))

        if (projects.isNotEmpty()) {
            Text(
                text = "PROJECTS",
                style = MaterialTheme.typography.labelSmall,
                color = TactileTheme.Primary
            )
            Spacer(modifier = Modifier.height(TactileTheme.SpacingSm))
            LazyColumn(
                modifier = Modifier.heightIn(max = 200.dp),
                verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm)
            ) {
                items(projects) { project ->
                    val projectNodes = allNodes.filter { it.node.projectId == project.id }
                    val total = projectNodes.size
                    val completed = projectNodes.count { it.node.status == "done" }
                    val progress = if (total > 0) completed.toFloat() / total else 0f

                    ProjectItem(
                        project,
                        progress,
                        total
                    ) { onNavigateToProject(project.id) }
                }
            }
            Spacer(modifier = Modifier.height(TactileTheme.SpacingMd))
        }

        Text(
            text = "DIRECT ITEMS",
            style = MaterialTheme.typography.labelSmall,
            color = TactileTheme.Primary
        )
        Spacer(modifier = Modifier.height(TactileTheme.SpacingSm))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm)) {
            items(nodesWithPin) { nodeWithPin ->
                NodeCard(
                    nodeWithPin = nodeWithPin,
                    onToggleDone = { status ->
                        viewModel.updateNodeStatus(
                            nodeWithPin.node,
                            status
                        )
                    },
                    onTogglePin = { isPinned -> viewModel.togglePin(nodeWithPin.node, isPinned) },
                    onLongClick = { onEditNode(nodeWithPin.node.id) },
                    onArchive = { viewModel.archiveNode(nodeWithPin.node) }
                )
            }
        }
    }
}
