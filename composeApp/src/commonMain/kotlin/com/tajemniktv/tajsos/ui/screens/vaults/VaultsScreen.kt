/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.vaults

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderShared
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.MedicalInformation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tajemniktv.tajsos.data.NodeWithPin
import com.tajemniktv.tajsos.ui.MainViewModel
import com.tajemniktv.tajsos.ui.VaultsSnapshot
import com.tajemniktv.tajsos.ui.screens.GroupedOpenLoopSection
import com.tajemniktv.tajsos.ui.theme.TactileTheme
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.Instant

@Composable
@OptIn(ExperimentalLayoutApi::class)
fun VaultsScreen(
    viewModel: MainViewModel,
    onEditNode: (Long) -> Unit,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val surface =
            if (maxWidth > 900.dp) VaultsDashboardSurface.DESKTOP else VaultsDashboardSurface.MOBILE
        val plan = remember(surface) { buildVaultsDashboardPlan(surface) }
        val context =
            remember(viewModel, onEditNode) { VaultsDashboardContext(viewModel, onEditNode) }
        Column(modifier = Modifier.fillMaxSize()) {
            plan.primary.forEach { block ->
                VaultsDashboardBlockRegistry.resolve(block.id)?.invoke(context)
            }
        }
    }
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
internal fun VaultsLayer(
    viewModel: MainViewModel,
    snapshot: VaultsSnapshot,
    onEditNode: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    var entryTitle by remember { mutableStateOf("") }
    var entryContent by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("vault_document") }
    var entryType by remember { mutableStateOf("document") }
    var searchQuery by remember { mutableStateOf("") }

    val categories =
        listOf(
            "vault_document" to "DOCUMENT VAULT",
            "vault_links" to "IMPORTANT LINKS",
            "vault_medical" to "MEDICAL INFO",
            "vault_university" to "UNIVERSITY INFO",
            "vault_ids_forms" to "IDs & FORMS",
            "vault_application_status" to "APPLICATION STATUS",
            "vault_receipts_paperwork" to "RECEIPTS / PAPERWORK",
            "vault_account_reference" to "ACCOUNT / REFERENCE",
            "vault_official_deadline" to "OFFICIAL DEADLINE",
        )

    val nowMillis = Clock.System.now().toEpochMilliseconds()
    val categoryCards =
        remember(snapshot, nowMillis) {
            listOf(
                VaultCategoryCardData(
                    title = "General Documents",
                    subtitle = "Core paperwork and operational docs.",
                    count = snapshot.documentVault.size,
                    icon = Icons.Default.Folder,
                    stamp = latestRelative(snapshot.documentVault, nowMillis),
                    badge = "ENCRYPTED",
                ),
                VaultCategoryCardData(
                    title = "Important Links",
                    subtitle = "Critical URLs and portal references.",
                    count = snapshot.importantLinksVault.size,
                    icon = Icons.Default.Link,
                    stamp = latestRelative(snapshot.importantLinksVault, nowMillis),
                    badge = "SYNCED",
                ),
                VaultCategoryCardData(
                    title = "Medical Records",
                    subtitle = "Health documents and provider details.",
                    count = snapshot.medicalInfoVault.size,
                    icon = Icons.Default.MedicalInformation,
                    stamp = latestRelative(snapshot.medicalInfoVault, nowMillis),
                    badge = "SECURE",
                ),
                VaultCategoryCardData(
                    title = "University",
                    subtitle = "Study IDs, policies, and references.",
                    count = snapshot.universityInfoVault.size,
                    icon = Icons.Default.FolderSpecial,
                    stamp = latestRelative(snapshot.universityInfoVault, nowMillis),
                ),
                VaultCategoryCardData(
                    title = "IDs & Forms",
                    subtitle = "Documents needed for verification.",
                    count = snapshot.idsAndFormsVault.size,
                    icon = Icons.Default.VerifiedUser,
                    stamp = latestRelative(snapshot.idsAndFormsVault, nowMillis),
                ),
                VaultCategoryCardData(
                    title = "Receipts & Paperwork",
                    subtitle = "Bureaucratic and financial trails.",
                    count = snapshot.receiptsPaperwork.size,
                    icon = Icons.Default.Description,
                    stamp = latestRelative(snapshot.receiptsPaperwork, nowMillis),
                ),
                VaultCategoryCardData(
                    title = "Accounts & Financials",
                    subtitle = "Non-sensitive account references.",
                    count = snapshot.accountReferenceVault.size,
                    icon = Icons.Default.AccountBalance,
                    stamp = latestRelative(snapshot.accountReferenceVault, nowMillis),
                    badge = "VAULTED",
                ),
                VaultCategoryCardData(
                    title = "Application Status",
                    subtitle = "Tracking visas, jobs, and external pipelines.",
                    count = snapshot.applicationStatusTracking.size,
                    icon = Icons.Default.FolderShared,
                    stamp = latestRelative(snapshot.applicationStatusTracking, nowMillis),
                ),
            )
        }

    val totalVaultItems = categoryCards.sumOf { it.count }
    val latestUpdatedAt =
        remember(snapshot) {
            listOf(
                snapshot.documentVault,
                snapshot.importantLinksVault,
                snapshot.medicalInfoVault,
                snapshot.universityInfoVault,
                snapshot.idsAndFormsVault,
                snapshot.applicationStatusTracking,
                snapshot.receiptsPaperwork,
                snapshot.accountReferenceVault,
                snapshot.officialDeadlineReminders,
            ).flatMap { it }.maxOfOrNull { it.node.updatedAt }
        }

    val filteredSections =
        remember(snapshot, searchQuery) {
            val query = searchQuery.trim().lowercase()
            buildSectionModels(snapshot).filter { section ->
                query.isBlank() ||
                    section.title.lowercase().contains(query) ||
                    section.items.any { it.lowercase().contains(query) }
            }
        }

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm),
    ) {
        item("vault_hero") {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = TactileTheme.VaultShell,
                shape = RoundedCornerShape(22.dp),
                border = BorderStroke(1.dp, TactileTheme.VaultBorder),
            ) {
                BoxWithConstraints {
                    val compact = maxWidth < 860.dp
                    Column(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .background(
                                    brush =
                                        Brush.linearGradient(
                                            colors =
                                                listOf(
                                                    TactileTheme.VaultGradientStart,
                                                    TactileTheme.VaultGradientMid,
                                                    TactileTheme.VaultGradientEnd,
                                                ),
                                        ),
                                ).padding(22.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            singleLine = true,
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                            label = { Text("Search vault") },
                        )
                        if (compact) {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                VaultHeroPrimary()
                                VaultHeroStats(
                                    totalVaultItems = totalVaultItems,
                                    latestUpdatedAt = latestUpdatedAt,
                                )
                            }
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Bottom,
                            ) {
                                VaultHeroPrimary(modifier = Modifier.weight(1f))
                                Spacer(Modifier.width(12.dp))
                                VaultHeroStats(
                                    totalVaultItems = totalVaultItems,
                                    latestUpdatedAt = latestUpdatedAt,
                                )
                            }
                        }
                        VaultDashboardCards(
                            cards = categoryCards,
                            applicationProgress = snapshot.applicationStatusTracking.size,
                            totalTracked = snapshot.applicationStatusTracking.size + snapshot.officialDeadlineReminders.size,
                        )
                        VaultEntryComposer(
                            entryTitle = entryTitle,
                            onEntryTitleChange = { entryTitle = it },
                            entryContent = entryContent,
                            onEntryContentChange = { entryContent = it },
                            selectedCategory = selectedCategory,
                            onSelectedCategoryChange = { selectedCategory = it },
                            categories = categories,
                            entryType = entryType,
                            onEntryTypeChange = { entryType = it },
                            onSave = {
                                viewModel.addVaultEntry(
                                    categoryTag = selectedCategory,
                                    title = entryTitle,
                                    content = entryContent,
                                    asType = entryType,
                                )
                                entryTitle = ""
                                entryContent = ""
                            },
                            onSaveApplicationStatus = {
                                viewModel.createApplicationStatusEntry(
                                    title = if (entryTitle.isBlank()) "Application status" else entryTitle,
                                    status = if (entryContent.isBlank()) "pending" else entryContent,
                                    dueAt =
                                        Clock.System
                                            .now()
                                            .toEpochMilliseconds() + (14L * 24 * 60 * 60 * 1000),
                                )
                                entryTitle = ""
                                entryContent = ""
                            },
                        )
                    }
                }
            }
        }

        items(filteredSections, key = { it.title }) { section ->
            GroupedOpenLoopSection(
                section.title,
                section.items,
            )
        }

        items(snapshot.mustFindLater, key = { it.node.id }) { item ->
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = TactileTheme.Surface,
                shape = RoundedCornerShape(TactileTheme.RadiusMd),
                border = BorderStroke(1.dp, TactileTheme.Primary),
            ) {
                Column(
                    modifier = Modifier.padding(TactileTheme.SpacingMd),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        item.node.title,
                        style = MaterialTheme.typography.titleSmall,
                        color = TactileTheme.Text,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        "Type ${item.node.type.uppercase()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TactileTheme.Muted,
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm),
                        verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm),
                    ) {
                        AssistChip(
                            onClick = { viewModel.markMustFindLater(item.node, false) },
                            label = { Text("UNMARK MUST-FIND-LATER") },
                        )
                        AssistChip(onClick = { onEditNode(item.node.id) }, label = { Text("OPEN") })
                    }
                }
            }
        }
    }
}

@Composable
private fun VaultHeroPrimary(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Surface(
            color = TactileTheme.Primary.copy(alpha = 0.14f),
            shape = RoundedCornerShape(999.dp),
            border = BorderStroke(1.dp, TactileTheme.Primary.copy(alpha = 0.35f)),
        ) {
            Text(
                "SYSTEM SECURE",
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                style = MaterialTheme.typography.labelSmall,
                color = TactileTheme.Primary,
            )
        }
        Text(
            "Persistent Document Vault",
            style = MaterialTheme.typography.displayMedium,
            color = TactileTheme.VaultTextStrong,
            fontWeight = FontWeight.ExtraBold,
        )
        Text(
            "Centralized repository for high-integrity assets. Data remains local-first while sync stays abstracted.",
            style = MaterialTheme.typography.bodyMedium,
            color = TactileTheme.VaultTextSubtle,
            modifier = Modifier.widthIn(max = 720.dp),
        )
    }
}

@Composable
private fun VaultHeroStats(
    totalVaultItems: Int,
    latestUpdatedAt: Long?,
) {
    Column(
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            "TOTAL CAPACITY",
            style = MaterialTheme.typography.labelSmall,
            color = TactileTheme.VaultTextSubtle,
        )
        Text(
            "$totalVaultItems ITEMS",
            style = MaterialTheme.typography.titleLarge,
            color = TactileTheme.VaultTextStrong,
            fontWeight = FontWeight.Bold,
        )
        Text(
            "LAST SYNC ${latestUpdatedAt?.let(::formatLocalTime) ?: "NO DATA"}",
            style = MaterialTheme.typography.labelSmall,
            color = TactileTheme.VaultTextAccent,
        )
    }
}

@Composable
private fun VaultDashboardCards(
    cards: List<VaultCategoryCardData>,
    applicationProgress: Int,
    totalTracked: Int,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val isDesktop = maxWidth > 980.dp
        val mainCard = cards.firstOrNull()
        val sideCards = cards.drop(1).take(6)
        val accountCard = cards.firstOrNull { it.title == "Accounts & Financials" }

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (isDesktop) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    if (mainCard != null) {
                        VaultCard(
                            data = mainCard,
                            modifier = Modifier.weight(1.5f).height(214.dp),
                            prominent = true,
                        )
                    }
                    FlowRow(
                        modifier = Modifier.weight(1f),
                        maxItemsInEachRow = 2,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        sideCards.forEach { card ->
                            VaultCard(
                                data = card,
                                modifier = Modifier.width(230.dp),
                            )
                        }
                    }
                }
            } else {
                if (mainCard != null) {
                    VaultCard(
                        data = mainCard,
                        modifier = Modifier.fillMaxWidth(),
                        prominent = true,
                    )
                }
                FlowRow(
                    maxItemsInEachRow = 2,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    sideCards.forEach { card ->
                        VaultCard(
                            data = card,
                            modifier = Modifier.fillMaxWidth(0.48f),
                        )
                    }
                }
            }

            ApplicationStatusCard(
                progress = applicationProgress,
                total = totalTracked,
            )

            if (accountCard != null) {
                VaultCard(
                    data = accountCard,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun VaultCard(
    data: VaultCategoryCardData,
    modifier: Modifier = Modifier,
    prominent: Boolean = false,
) {
    Card(
        modifier = modifier,
        colors =
            CardDefaults.cardColors(
                containerColor =
                    if (prominent) TactileTheme.VaultSoft else TactileTheme.VaultShell,
            ),
        shape = RoundedCornerShape(if (prominent) 18.dp else 14.dp),
        border = BorderStroke(1.dp, TactileTheme.VaultBorder),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier =
                        Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(TactileTheme.Primary.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(data.icon, contentDescription = null, tint = TactileTheme.Primary)
                }
                if (data.badge != null) {
                    Surface(
                        color = TactileTheme.Surface.copy(alpha = 0.45f),
                        shape = RoundedCornerShape(999.dp),
                        border = BorderStroke(1.dp, TactileTheme.VaultBorder),
                    ) {
                        Text(
                            data.badge,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = TactileTheme.VaultTextSubtle,
                        )
                    }
                }
            }
            Text(
                data.title,
                style = MaterialTheme.typography.titleMedium,
                color = TactileTheme.VaultTextStrong,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                data.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = TactileTheme.VaultTextSubtle,
            )
            Spacer(Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    "${data.count} items",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TactileTheme.VaultTextStrong,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    data.stamp,
                    style = MaterialTheme.typography.labelSmall,
                    color = TactileTheme.VaultTextAccent,
                )
            }
        }
    }
}

@Composable
private fun ApplicationStatusCard(
    progress: Int,
    total: Int,
) {
    val safeTotal = if (total <= 0) 1 else total
    val progressRatio = progress.toFloat() / safeTotal.toFloat()

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = TactileTheme.VaultSoft),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, TactileTheme.VaultBorder),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                "Application Status",
                style = MaterialTheme.typography.titleMedium,
                color = TactileTheme.VaultTextStrong,
            )
            Text(
                "Live tracking for visas, jobs, housing and authority processes.",
                style = MaterialTheme.typography.bodySmall,
                color = TactileTheme.VaultTextSubtle,
            )
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(TactileTheme.Background.copy(alpha = 0.65f)),
            ) {
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth(progressRatio.coerceIn(0f, 1f))
                            .height(6.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(TactileTheme.Primary),
                )
            }
            Text(
                "$progress active records",
                style = MaterialTheme.typography.labelSmall,
                color = TactileTheme.VaultTextSubtle,
            )
        }
    }
}

@Composable
private fun VaultEntryComposer(
    entryTitle: String,
    onEntryTitleChange: (String) -> Unit,
    entryContent: String,
    onEntryContentChange: (String) -> Unit,
    selectedCategory: String,
    onSelectedCategoryChange: (String) -> Unit,
    categories: List<Pair<String, String>>,
    entryType: String,
    onEntryTypeChange: (String) -> Unit,
    onSave: () -> Unit,
    onSaveApplicationStatus: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = TactileTheme.VaultShell),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, TactileTheme.VaultBorder),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                "Quick Upload",
                style = MaterialTheme.typography.titleSmall,
                color = TactileTheme.VaultTextStrong,
            )
            OutlinedTextField(
                value = entryTitle,
                onValueChange = onEntryTitleChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Entry title") },
                singleLine = true,
            )
            OutlinedTextField(
                value = entryContent,
                onValueChange = onEntryContentChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Details / reference") },
                minLines = 2,
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm),
                verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm),
            ) {
                categories.forEach { (key, label) ->
                    FilterChip(
                        selected = selectedCategory == key,
                        onClick = { onSelectedCategoryChange(key) },
                        label = { Text(label) },
                    )
                }
            }
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm),
                verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm),
            ) {
                listOf("document", "resource", "note", "vault").forEach { type ->
                    FilterChip(
                        selected = entryType == type,
                        onClick = { onEntryTypeChange(type) },
                        label = { Text(type.uppercase()) },
                    )
                }
            }
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm),
                verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm),
            ) {
                AssistChip(
                    onClick = onSave,
                    label = { Text("SAVE ENTRY") },
                )
                AssistChip(
                    onClick = onSaveApplicationStatus,
                    label = { Text("APP STATUS +14D") },
                )
            }
        }
    }
}

@Immutable
private data class VaultCategoryCardData(
    val title: String,
    val subtitle: String,
    val count: Int,
    val icon: ImageVector,
    val stamp: String,
    val badge: String? = null,
)

@Immutable
private data class VaultSectionModel(
    val title: String,
    val items: List<String>,
)

private fun buildSectionModels(snapshot: VaultsSnapshot): List<VaultSectionModel> =
    listOf(
        VaultSectionModel("DOCUMENT VAULT", snapshot.documentVault.map { it.node.title }),
        VaultSectionModel("IMPORTANT LINKS VAULT", snapshot.importantLinksVault.map { it.node.title }),
        VaultSectionModel("MEDICAL INFO VAULT", snapshot.medicalInfoVault.map { it.node.title }),
        VaultSectionModel("UNIVERSITY INFO VAULT", snapshot.universityInfoVault.map { it.node.title }),
        VaultSectionModel("IDs / FORMS VAULT", snapshot.idsAndFormsVault.map { it.node.title }),
        VaultSectionModel("APPLICATION STATUS TRACKING", snapshot.applicationStatusTracking.map { it.node.title }),
        VaultSectionModel("RECEIPTS / PAPERWORK", snapshot.receiptsPaperwork.map { it.node.title }),
        VaultSectionModel("ACCOUNT / REFERENCE VAULT", snapshot.accountReferenceVault.map { it.node.title }),
        VaultSectionModel("OFFICIAL DEADLINE REMINDERS", snapshot.officialDeadlineReminders.map { it.node.title }),
    ).filter { it.items.isNotEmpty() }

private fun latestRelative(
    items: List<NodeWithPin>,
    nowMillis: Long,
): String {
    val latest = items.maxOfOrNull { it.node.updatedAt } ?: return "No updates"
    val diff = (nowMillis - latest).coerceAtLeast(0)
    val hour = 60 * 60 * 1000L
    val day = 24 * hour

    return when {
        diff < hour -> "Today"
        diff < day -> "${diff / hour}h ago"
        diff < day * 7 -> "${diff / day}d ago"
        else -> formatLocalDate(latest)
    }
}

private fun formatLocalDate(timestamp: Long): String {
    val local = Instant.fromEpochMilliseconds(timestamp).toLocalDateTime(TimeZone.currentSystemDefault())
    val day = local.day.toString().padStart(2, '0')
    val month = (local.month.ordinal + 1).toString().padStart(2, '0')
    return "$day/$month/${local.year.toString().takeLast(2)}"
}

private fun formatLocalTime(timestamp: Long): String {
    val local = Instant.fromEpochMilliseconds(timestamp).toLocalDateTime(TimeZone.currentSystemDefault())
    val hour = local.hour.toString().padStart(2, '0')
    val minute = local.minute.toString().padStart(2, '0')
    return "$hour:$minute"
}
