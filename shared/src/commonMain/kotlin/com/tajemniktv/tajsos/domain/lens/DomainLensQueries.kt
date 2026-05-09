/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.domain.lens

import com.tajemniktv.tajsos.data.NodeWithPin
import com.tajemniktv.tajsos.data.isKnowledgeItem
import com.tajemniktv.tajsos.data.isTaskItem
import com.tajemniktv.tajsos.ui.MaintenanceSnapshot
import com.tajemniktv.tajsos.ui.MaintenanceStatusItem

/**
 * Explicit maintenance item types that classify as financial responsibilities.
 */
private val financeMaintenanceTypes = setOf("bill", "subscription", "renewal")

/**
 * Explicit maintenance item types that classify as health and medical responsibilities.
 */
private val healthMaintenanceTypes = setOf("appointment", "prescription", "med_refill")

/**
 * Set of normalized tag names used to heuristically detect finance-related items.
 */
private val financeTagMarkers =
    setOf(
        "finance",
        "money",
        "budget",
        "bill",
        "subscription",
        "renewal",
        "invoice",
        "payment",
        "rent",
        "tax",
        "account",
        "bank",
        "receipt",
        "insurance",
    )

/**
 * List of substring keywords used to search titles and content for financial context.
 */
private val financeTitleKeywords =
    listOf(
        "finance",
        "budget",
        "bill",
        "subscription",
        "renewal",
        "invoice",
        "payment",
        "rent",
        "tax",
        "account",
        "bank",
        "receipt",
        "insurance",
        "salary",
        "paycheck",
    )

/**
 * Set of normalized tag names used to heuristically detect health-related items.
 */
private val healthTagMarkers =
    setOf(
        "health",
        "medical",
        "symptom",
        "therapy",
        "doctor",
        "medication",
        "wellbeing",
        "mental_health",
    )

/**
 * List of substring keywords used to search titles and content for health and medical context.
 */
private val healthTitleKeywords =
    listOf(
        "health",
        "medical",
        "symptom",
        "therapy",
        "doctor",
        "prescription",
        "medication",
        "appointment",
    )

/**
 * Shared projection helpers for domain screens.
 *
 * These helpers keep query logic out of UI composables and avoid pushing more orchestration
 * into [com.tajemniktv.tajsos.ui.MainViewModel].
 *
 * Domains (such as finance or health) are categorized implicitly by checking for hardcoded
 * string markers within node tags, titles, content, `maintenanceType`, and `noteType` fields.
 * This heuristic-based approach provides a zero-configuration experience, allowing items to be surfaced
 * appropriately even if the user forgets to manually assign the domain.
 *
 * Note: These queries intentionally bypass explicit domain associations (e.g., via `associatedDomains`
 * in `AreaMetadata`) in favor of terminology matching to lower the friction of capturing new data.
 *
 * This object implements a zero-configuration classification strategy. Instead of relying on
 * explicit database associations (like a many-to-many domain relation table), items are
 * implicitly categorized into domains via keyword matching in titles, content, tags, and
 * specific maintenance/note types. This decoupling allows items to naturally surface in the
 * right lenses without requiring manual user curation.
 *
 * Currently, heuristic matching queries are only implemented for the `FINANCES` and `HEALTH` domains.
 * `EDUCATION` and `RELATIONSHIPS` (defined in [com.tajemniktv.tajsos.domain.DomainKind])
 * do not yet have dedicated queries in this object.
 */
object DomainLensQueries {
    /**
     * Returns active maintenance items that belong to the finance domain.
     * Filters the snapshot's active items for those where `maintenanceType` is within `financeMaintenanceTypes`.
     *
     * @param snapshot The snapshot containing current maintenance items separated by their schedule state.
     * @return A list of active items whose maintenance type implies financial responsibility.
     */
    fun financeMaintenanceItems(snapshot: MaintenanceSnapshot): List<MaintenanceStatusItem> =
        snapshot.active.filter { it.node.node.maintenanceType in financeMaintenanceTypes }

    /**
     * Returns recurring finance-related maintenance commitments.
     * Filters the snapshot's recurring items for those where `maintenanceType` is within `financeMaintenanceTypes`.
     *
     * @param snapshot The snapshot containing current maintenance items separated by their schedule state.
     * @return A list of recurring items whose maintenance type implies financial responsibility.
     */
    fun financeRecurringItems(snapshot: MaintenanceSnapshot): List<MaintenanceStatusItem> =
        snapshot.recurring.filter { it.node.node.maintenanceType in financeMaintenanceTypes }

    /**
     * Returns overdue finance-related maintenance work.
     * Filters the snapshot's overdue items for those where `maintenanceType` is within `financeMaintenanceTypes`.
     *
     * @param snapshot The snapshot containing current maintenance items separated by their schedule state.
     * @return A list of overdue items whose maintenance type implies financial responsibility.
     */
    fun financeOverdueItems(snapshot: MaintenanceSnapshot): List<MaintenanceStatusItem> =
        snapshot.overdue.filter { it.node.node.maintenanceType in financeMaintenanceTypes }

    /**
     * Returns active task-shaped work that reads as finance-related.
     * Filters nodes to include only active tasks that match the `matchesFinanceSignal` heuristics.
     * The results are sorted by approaching deadline (nodes without deadlines are placed at the end).
     *
     * @param nodes A flat list of nodes wrapped with their today-pin context.
     * @return A list of active task nodes matching finance heuristics, sorted by approaching deadline.
     */
    fun financeActionItems(nodes: List<NodeWithPin>): List<NodeWithPin> =
        nodes
            .filter { it.node.isTaskItem() && it.node.status == "active" }
            .filter(::matchesFinanceSignal)
            .sortedBy { it.node.dueAt ?: Long.MAX_VALUE }

    /**
     * Returns durable finance notes and records worth surfacing in the finance lens.
     * Filters nodes to include only active knowledge items that match the `matchesFinanceSignal` heuristics.
     * The results are sorted by their most recent update time.
     *
     * @param nodes A flat list of nodes wrapped with their today-pin context.
     * @return A list of active knowledge nodes matching finance heuristics, sorted by most recently updated.
     */
    fun financeKnowledgeItems(nodes: List<NodeWithPin>): List<NodeWithPin> =
        nodes
            .filter { it.node.isKnowledgeItem() && it.node.status == "active" }
            .filter(::matchesFinanceSignal)
            .sortedByDescending { it.node.updatedAt }

    /**
     * Returns finance-related items with explicit dates, regardless of whether they are tasks or notes.
     * Filters active nodes to include only those with explicit due dates matching the `matchesFinanceSignal` heuristics.
     * The results are sorted by approaching deadline.
     *
     * @param nodes A flat list of nodes wrapped with their today-pin context.
     * @return A list of active, scheduled nodes matching finance heuristics, sorted by approaching deadline.
     */
    fun financeDeadlineItems(nodes: List<NodeWithPin>): List<NodeWithPin> =
        nodes
            .filter { it.node.status == "active" && it.node.dueAt != null }
            .filter(::matchesFinanceSignal)
            .sortedBy { it.node.dueAt ?: Long.MAX_VALUE }

    /**
     * Returns active maintenance items that belong to the health domain.
     * Filters the snapshot's active items for those where `maintenanceType` is within `healthMaintenanceTypes`.
     *
     * @param snapshot The snapshot containing current maintenance items separated by their schedule state.
     * @return A list of active items whose maintenance type implies health-related responsibility.
     */
    fun healthMaintenanceItems(snapshot: MaintenanceSnapshot): List<MaintenanceStatusItem> =
        snapshot.active.filter { it.node.node.maintenanceType in healthMaintenanceTypes }

    /**
     * Returns recurring health-related maintenance commitments.
     * Filters the snapshot's recurring items for those where `maintenanceType` is within `healthMaintenanceTypes`.
     *
     * @param snapshot The snapshot containing current maintenance items separated by their schedule state.
     * @return A list of recurring items whose maintenance type implies health-related responsibility.
     */
    fun healthRecurringItems(snapshot: MaintenanceSnapshot): List<MaintenanceStatusItem> =
        snapshot.recurring.filter { it.node.node.maintenanceType in healthMaintenanceTypes }

    /**
     * Returns overdue health-related maintenance work.
     * Filters the snapshot's overdue items for those where `maintenanceType` is within `healthMaintenanceTypes`.
     *
     * @param snapshot The snapshot containing current maintenance items separated by their schedule state.
     * @return A list of overdue items whose maintenance type implies health-related responsibility.
     */
    fun healthOverdueItems(snapshot: MaintenanceSnapshot): List<MaintenanceStatusItem> =
        snapshot.overdue.filter { it.node.node.maintenanceType in healthMaintenanceTypes }

    /**
     * Returns active task-shaped work that reads as health-related.
     * Filters nodes to include only active tasks that match the `matchesHealthSignal` heuristics.
     * The results are sorted by approaching deadline (nodes without deadlines are placed at the end).
     *
     * @param nodes A flat list of nodes wrapped with their today-pin context.
     * @return A list of active task nodes matching health heuristics, sorted by approaching deadline.
     */
    fun healthActionItems(nodes: List<NodeWithPin>): List<NodeWithPin> =
        nodes
            .filter { it.node.isTaskItem() && it.node.status == "active" }
            .filter(::matchesHealthSignal)
            .sortedBy { it.node.dueAt ?: Long.MAX_VALUE }

    /**
     * Returns active health notes and records worth surfacing in the health lens.
     * Filters nodes to include only active knowledge items that match the `matchesHealthSignal` heuristics.
     * The results are sorted by their most recent update time.
     *
     * @param nodes A flat list of nodes wrapped with their today-pin context.
     * @return A list of active knowledge nodes matching health heuristics, sorted by most recently updated.
     */
    fun healthKnowledgeItems(nodes: List<NodeWithPin>): List<NodeWithPin> =
        nodes
            .filter { it.node.isKnowledgeItem() && it.node.status == "active" }
            .filter(::matchesHealthSignal)
            .sortedByDescending { it.node.updatedAt }

    /**
     * Determines whether a node implicitly belongs to the finance domain based on tags,
     * keywords in title/content, maintenance type, or note type (e.g., reference notes).
     *
     * Classification uses hardcoded marker constants ([financeTagMarkers],
     * [financeTitleKeywords], and [financeMaintenanceTypes]) plus non-constant node-state
     * checks such as reference-note matching (`noteType == "reference"`).
     *
     * Note: This intentionally bypasses explicit `ItemDomainEntity` database associations
     * to provide a zero-configuration experience, ensuring finance items are surfaced even
     * if the user forgets to manually assign the finance domain. This heuristic-based logic
     * relies on implicit keyword matching to decouple domain categorization from explicit user action.
     */
    private fun matchesFinanceSignal(node: NodeWithPin): Boolean {
        val title = node.node.title.lowercase()
        val content = node.node.content.lowercase()
        val hasFinanceTag = node.tags.any { it.normalizedName in financeTagMarkers }
        val mentionsFinanceTitle = financeTitleKeywords.any { keyword -> title.contains(keyword) }
        val mentionsFinanceContent =
            financeTitleKeywords.any { keyword -> content.contains(keyword) }
        val financeMaintenance = node.node.maintenanceType in financeMaintenanceTypes
        val referenceFinanceNote =
            node.node.noteType == "reference" && (mentionsFinanceTitle || hasFinanceTag)
        return hasFinanceTag || mentionsFinanceTitle || mentionsFinanceContent || financeMaintenance || referenceFinanceNote
    }

    /**
     * Determines whether a node implicitly belongs to the health domain based on tags,
     * keywords in title/content, maintenance type, or note type (e.g., reflections).
     *
     * Classification relies on checking against hardcoded marker constants ([healthTagMarkers],
     * [healthTitleKeywords], [healthMaintenanceTypes]) and specific note types (e.g., "reflection").
     *
     * Note: This intentionally bypasses explicit `ItemDomainEntity` database associations
     * to provide a zero-configuration experience, ensuring health items are surfaced even
     * if the user forgets to manually assign the health domain. This heuristic-based logic
     * relies on implicit keyword matching to decouple domain categorization from explicit user action.
     */
    private fun matchesHealthSignal(node: NodeWithPin): Boolean {
        val title = node.node.title.lowercase()
        val content = node.node.content.lowercase()
        val hasHealthTag = node.tags.any { it.normalizedName in healthTagMarkers }
        val mentionsHealthTitle = healthTitleKeywords.any { keyword -> title.contains(keyword) }
        val mentionsHealthContent = healthTitleKeywords.any { keyword -> content.contains(keyword) }
        val healthNoteType = node.node.noteType in setOf("reflection", "journal")
        val healthMaintenance = node.node.maintenanceType in healthMaintenanceTypes
        return hasHealthTag || mentionsHealthTitle || mentionsHealthContent || healthMaintenance || healthNoteType
    }
}
