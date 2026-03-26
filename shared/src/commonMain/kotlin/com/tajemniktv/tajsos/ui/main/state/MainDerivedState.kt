/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui

import com.tajemniktv.tajsos.data.NodeWithPin

data class PlaceLogisticsItem(
    val place: NodeWithPin,
    val relatedTasks: List<NodeWithPin>,
    val remindersCount: Int,
)

data class PhysicalLogisticsSnapshot(
    val places: List<PlaceLogisticsItem> = emptyList(),
    val campusLocations: List<PlaceLogisticsItem> = emptyList(),
    val homeZones: List<PlaceLogisticsItem> = emptyList(),
    val placeBasedTasks: List<NodeWithPin> = emptyList(),
    val outOfHomeTaskClusters: Map<String, List<NodeWithPin>> = emptyMap(),
    val errandClusters: Map<String, List<NodeWithPin>> = emptyMap(),
    val whatToBringLists: List<NodeWithPin> = emptyList(),
    val packingLists: List<NodeWithPin> = emptyList(),
    val leaveHomeChecklists: List<NodeWithPin> = emptyList(),
    val dontForgetSets: List<NodeWithPin> = emptyList(),
    val eventPreparationLists: List<NodeWithPin> = emptyList(),
    val classSpecificBringLists: List<NodeWithPin> = emptyList(),
    val physicalLogisticsNotes: List<NodeWithPin> = emptyList(),
    val travelPackTemplateReady: Boolean = false,
    val locationSpecificReminders: List<NodeWithPin> = emptyList(),
)

data class PersonalRulesSnapshot(
    val vault: List<NodeWithPin> = emptyList(),
    val antiGoals: List<NodeWithPin> = emptyList(),
    val redFlags: List<NodeWithPin> = emptyList(),
    val greenFlags: List<NodeWithPin> = emptyList(),
    val priorities: List<NodeWithPin> = emptyList(),
    val tendToForget: List<NodeWithPin> = emptyList(),
    val messesMeUp: List<NodeWithPin> = emptyList(),
    val helpsOffBalance: List<NodeWithPin> = emptyList(),
    val decisionPrinciples: List<NodeWithPin> = emptyList(),
    val constraints: List<NodeWithPin> = emptyList(),
    val foundationalRules: List<NodeWithPin> = emptyList(),
    val recoveryReminders: List<NodeWithPin> = emptyList(),
    val distrustBrainNotes: List<NodeWithPin> = emptyList(),
    val whatWorksNotes: List<NodeWithPin> = emptyList(),
    val pinnedPrinciples: List<NodeWithPin> = emptyList(),
    val playbookLinksCount: Int = 0,
)

data class VaultsSnapshot(
    val documentVault: List<NodeWithPin> = emptyList(),
    val importantLinksVault: List<NodeWithPin> = emptyList(),
    val medicalInfoVault: List<NodeWithPin> = emptyList(),
    val universityInfoVault: List<NodeWithPin> = emptyList(),
    val idsAndFormsVault: List<NodeWithPin> = emptyList(),
    val applicationStatusTracking: List<NodeWithPin> = emptyList(),
    val receiptsPaperwork: List<NodeWithPin> = emptyList(),
    val accountReferenceVault: List<NodeWithPin> = emptyList(),
    val officialDeadlineReminders: List<NodeWithPin> = emptyList(),
    val mustFindLater: List<NodeWithPin> = emptyList(),
)

data class LoadTrendPoint(
    val label: String,
    val load: Int,
    val fragmentation: Int,
)

data class CapacitySnapshot(
    val loadScore: Int = 0,
    val fragmentationScore: Int = 0,
    val tooManyActiveProjectsWarning: String? = null,
    val adminDebtWarning: String? = null,
    val openLoopsOverloadWarning: String? = null,
    val capacityMismatch: String? = null,
    val unrealisticWeekSignal: String? = null,
    val tooManyActiveFrontsIndicator: String? = null,
    val attentionFragmentedIndicator: String? = null,
    val weeklyStructuralOverloadWarning: String? = null,
    val loadByArea: Map<Long?, Int> = emptyMap(),
    val loadByMode: Map<String, Int> = emptyMap(),
    val loadTrend: List<LoadTrendPoint> = emptyList(),
    val capacityAwareSuggestions: List<String> = emptyList(),
)

data class LifeOSSignatureSnapshot(
    val operatingModesEnabled: Boolean = false,
    val areaHealthEnabled: Boolean = false,
    val openLoopsEnabled: Boolean = false,
    val decisionSystemEnabled: Boolean = false,
    val maintenanceEnabled: Boolean = false,
    val contextAwareFilteringEnabled: Boolean = false,
    val transitionProtocolsEnabled: Boolean = false,
    val recoveryModeEnabled: Boolean = false,
    val relationshipLayerEnabled: Boolean = false,
    val logisticsVaultEnabled: Boolean = false,
    val loadCapacityEnabled: Boolean = false,
    val personalPrinciplesPlaybooksEnabled: Boolean = false,
    val modeOfLifeLabel: String = "unknown",
    val modeOfLifeReason: String = "",
    val workDateDueCoveragePercent: Int = 0,
    val workDateDueItems: List<NodeWithPin> = emptyList(),
)

data class DistinctionQuestionState(
    val question: String,
    val answer: String,
    val answered: Boolean = true,
)

data class LifeOSSecondBrainSnapshot(
    val secondBrainQuestions: List<DistinctionQuestionState> = emptyList(),
    val lifeOSQuestions: List<DistinctionQuestionState> = emptyList(),
    val secondBrainCoveragePercent: Int = 0,
    val lifeOSCoveragePercent: Int = 0,
    val postureLabel: String = "underconfigured",
)

data class DirectionCommitmentStatus(
    val commitment: String,
    val satisfied: Boolean,
    val evidence: String,
)

data class CombinedDirectionSnapshot(
    val commitments: List<DirectionCommitmentStatus> = emptyList(),
    val completionPercent: Int = 0,
    val practicalitySignals: List<String> = emptyList(),
    val postureLabel: String = "underconfigured",
)

data class CoreLifeOSShiftItem(
    val criterion: String,
    val satisfied: Boolean,
    val evidence: String,
)

data class CoreLifeOSShiftSnapshot(
    val items: List<CoreLifeOSShiftItem> = emptyList(),
    val completionPercent: Int = 0,
    val connectedProperly: Boolean = false,
    val integrationWarning: String? = null,
)
