package com.tajemniktv.tajsos.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.tajemniktv.tajsos.ui.MainViewModel
import com.tajemniktv.tajsos.ui.theme.TactileTheme

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SearchScreen(
    viewModel: MainViewModel,
    onItemClick: (Long) -> Unit
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(TactileTheme.SpacingMd)) {
        TextField(
            value = searchQuery,
            onValueChange = { viewModel.updateSearchQuery(it) },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search items...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = TactileTheme.Surface,
                unfocusedContainerColor = TactileTheme.Surface
            )
        )

        Spacer(modifier = Modifier.height(TactileTheme.SpacingMd))

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(searchResults) { nodeWithPin ->
                ListItem(
                    headlineContent = { Text(nodeWithPin.node.title) },
                    supportingContent = { Text(nodeWithPin.node.type.uppercase()) },
                    modifier = Modifier.combinedClickable(
                        onClick = { onItemClick(nodeWithPin.node.id) },
                        onLongClick = { onItemClick(nodeWithPin.node.id) }
                    ),
                    colors = ListItemDefaults.colors(containerColor = TactileTheme.Surface)
                )
                HorizontalDivider(color = TactileTheme.Muted.copy(alpha = 0.5f))
            }
        }
    }
}
