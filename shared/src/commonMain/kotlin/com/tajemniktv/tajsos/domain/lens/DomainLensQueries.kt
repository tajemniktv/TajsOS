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
 *
 * This set is used in heuristic matching to implicitly map items with these maintenance
 * types into the finance domain without requiring an explicit [com.tajemniktv.tajsos.domain.DomainKind] assignment.
 */
private val financeMaintenanceTypes = setOf("bill", "subscription", "renewal")

/**
 * Explicit maintenance item types that classify as health and medical responsibilities.
 *
 * This set is used in heuristic matching to implicitly map items with these maintenance
 * types into the health domain without requiring an explicit [com.tajemniktv.tajsos.domain.DomainKind] assignment.
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
 * Provides static heuristic queries to categorize active nodes into broader LifeOS domains.
 *
 * This singleton relies heavily on zero-configuration implicit matching—such as tags,
 * keywords within title and content, maintenance types, and specific note types—rather than explicit database
 * entity associations. This design significantly lowers the friction of capturing new items,
 * ensuring they surface in relevant domains (like Finances or Health) automatically.
 *
 * This object evaluates the system's shared nodes (tasks, notes, records) and determines
 * if they implicitly belong to a specific LifeOS domain (like Finance or Health) based on
 * explicit markers such as tags, titles, content keywords, maintenance types, or specific
 * note types (e.g., reflections vs references).
 *
 * This zero-configuration design intentionally bypasses strict associative models
 * (like `ItemDomainEntity`) so users are not forced to explicitly classify every
 * inbound task to make the lens surfaces function properly.
 *
 * Currently, heuristic matching queries are only implemented for the `FINANCES` and `HEALTH` domains.
 * `EDUCATION` and `RELATIONSHIPS` (defined in [com.tajemniktv.tajsos.domain.DomainKind])
 * do not yet have dedicated queries in this object. Adding support for them would require
 * establishing similar heuristic marker sets (e.g., `educationTagMarkers`, `educationTitleKeywords`,
 * `relationshipsMaintenanceTypes`) and implementing their respective projection queries.
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
     * Common heuristic matching logic to avoid code duplication.
     */
    private fun matchesDomainSignal(
        node: NodeWithPin,
        maintenanceTypes: Set<String>,
        tagMarkers: Set<String>,
        titleKeywords: List<String>,
        validNoteTypes: Set<String> = emptySet()
    ): Boolean {
        if (node.node.maintenanceType in maintenanceTypes) return true

        val noteType = node.node.noteType
        if (noteType != null && noteType in validNoteTypes) return true

        if (node.tags.any { it.normalizedName in tagMarkers }) return true

        val title = node.node.title.lowercase()
        if (titleKeywords.any { keyword -> title.contains(keyword) }) return true

        val content = node.node.content.lowercase()
        if (titleKeywords.any { keyword -> content.contains(keyword) }) return true

        return false
    }

    /**
     * Determines whether a node implicitly belongs to the finance domain based on tags,
     * keywords in title/content, maintenance type, or note type (e.g., reference notes).
     *
     * Classification uses hardcoded marker constants ([financeTagMarkers],
     * [financeTitleKeywords], and [financeMaintenanceTypes]).
     *
     * Note: This intentionally bypasses explicit `ItemDomainEntity` database associations
     * to provide a zero-configuration experience, ensuring finance items are surfaced even
     * if the user forgets to manually assign the finance domain. This heuristic-based logic
     * relies on implicit keyword matching to decouple domain categorization from explicit user action.
     */
    private fun matchesFinanceSignal(node: NodeWithPin): Boolean =
        matchesDomainSignal(node, financeMaintenanceTypes, financeTagMarkers, financeTitleKeywords)

    private val healthNoteTypes = setOf("reflection", "journal")

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
    private fun matchesHealthSignal(node: NodeWithPin): Boolean =
        matchesDomainSignal(node, healthMaintenanceTypes, healthTagMarkers, healthTitleKeywords, healthNoteTypes)
}
