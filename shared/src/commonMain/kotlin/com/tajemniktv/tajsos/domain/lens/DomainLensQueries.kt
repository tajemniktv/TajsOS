/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.domain.lens

import com.tajemniktv.tajsos.data.NodeWithPin
import com.tajemniktv.tajsos.data.isKnowledgeItem
import com.tajemniktv.tajsos.data.isTaskItem
import com.tajemniktv.tajsos.ui.MaintenanceSnapshot
import com.tajemniktv.tajsos.ui.MaintenanceStatusItem

private val financeMaintenanceTypes = setOf("bill", "subscription", "renewal")
private val healthMaintenanceTypes = setOf("appointment", "prescription", "med_refill")
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
 * into MainViewModel.
 */
object DomainLensQueries {
    /**
     * Returns active maintenance items that belong to the finance domain.
     */
    fun financeMaintenanceItems(snapshot: MaintenanceSnapshot): List<MaintenanceStatusItem> =
        snapshot.active.filter { it.node.node.maintenanceType in financeMaintenanceTypes }

    /**
     * Returns recurring finance-related maintenance commitments.
     */
    fun financeRecurringItems(snapshot: MaintenanceSnapshot): List<MaintenanceStatusItem> =
        snapshot.recurring.filter { it.node.node.maintenanceType in financeMaintenanceTypes }

    /**
     * Returns overdue finance-related maintenance work.
     */
    fun financeOverdueItems(snapshot: MaintenanceSnapshot): List<MaintenanceStatusItem> =
        snapshot.overdue.filter { it.node.node.maintenanceType in financeMaintenanceTypes }

    /**
     * Returns active task-shaped work that reads as finance-related.
     */
    fun financeActionItems(nodes: List<NodeWithPin>): List<NodeWithPin> =
        nodes
            .filter { it.node.isTaskItem() && it.node.status == "active" }
            .filter(::matchesFinanceSignal)
            .sortedBy { it.node.dueAt ?: Long.MAX_VALUE }

    /**
     * Returns durable finance notes and records worth surfacing in the finance lens.
     */
    fun financeKnowledgeItems(nodes: List<NodeWithPin>): List<NodeWithPin> =
        nodes
            .filter { it.node.isKnowledgeItem() && it.node.status == "active" }
            .filter(::matchesFinanceSignal)
            .sortedByDescending { it.node.updatedAt }

    /**
     * Returns finance-related items with explicit dates, regardless of whether they are tasks or notes.
     */
    fun financeDeadlineItems(nodes: List<NodeWithPin>): List<NodeWithPin> =
        nodes
            .filter { it.node.status == "active" && it.node.dueAt != null }
            .filter(::matchesFinanceSignal)
            .sortedBy { it.node.dueAt ?: Long.MAX_VALUE }

    fun healthMaintenanceItems(snapshot: MaintenanceSnapshot): List<MaintenanceStatusItem> =
        snapshot.active.filter { it.node.node.maintenanceType in healthMaintenanceTypes }

    fun healthOverdueItems(snapshot: MaintenanceSnapshot): List<MaintenanceStatusItem> =
        snapshot.overdue.filter { it.node.node.maintenanceType in healthMaintenanceTypes }

    fun healthActionItems(nodes: List<NodeWithPin>): List<NodeWithPin> =
        nodes
            .filter { it.node.isTaskItem() && it.node.status == "active" }
            .filter(::matchesHealthSignal)
            .sortedBy { it.node.dueAt ?: Long.MAX_VALUE }

    fun healthKnowledgeItems(nodes: List<NodeWithPin>): List<NodeWithPin> =
        nodes
            .filter { it.node.isKnowledgeItem() && it.node.status == "active" }
            .filter(::matchesHealthSignal)
            .sortedByDescending { it.node.updatedAt }

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
