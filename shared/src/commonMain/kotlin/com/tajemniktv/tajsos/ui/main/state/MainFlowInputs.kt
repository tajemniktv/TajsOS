/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui

import com.tajemniktv.tajsos.data.ModeEntity
import com.tajemniktv.tajsos.data.NodeEntity
import com.tajemniktv.tajsos.data.NodeWithPin
import com.tajemniktv.tajsos.data.RelationEntity
import com.tajemniktv.tajsos.data.TrackEntryEntity

/**
 * An internal data class bundling the primary inputs required to calculate the [CapacitySnapshot].
 *
 * @param nodes The complete list of active nodes in the system.
 * @param projects The complete list of active project entities.
 * @param areas The complete list of active area entities.
 * @param maintenance The current [MaintenanceSnapshot] evaluating chore debt.
 * @param openLoops The current [OpenLoopsSnapshot] evaluating unresolved inputs.
 */
internal data class CapacityInputs(
    val nodes: List<NodeWithPin>,
    val projects: List<NodeEntity>,
    val areas: List<NodeEntity>,
    val maintenance: MaintenanceSnapshot,
    val openLoops: OpenLoopsSnapshot,
)

/**
 * An internal data class bundling the core entity inputs required for LifeOS Signature calculation.
 *
 * @param modes The complete list of user-defined focus modes.
 * @param areaHealth The current [AreaHealthSnapshot] detailing area stability.
 * @param openLoops The current [OpenLoopsSnapshot] detailing cognitive load.
 * @param maintenance The current [MaintenanceSnapshot] detailing routine debt.
 * @param relationships The current [RelationshipSnapshot] detailing social obligations.
 */
internal data class LifeOSSignatureInputs(
    val modes: List<ModeEntity>,
    val areaHealth: AreaHealthSnapshot,
    val openLoops: OpenLoopsSnapshot,
    val maintenance: MaintenanceSnapshot,
    val relationships: RelationshipSnapshot,
)

/**
 * An internal data class bundling the inputs alongside broader context snapshots for LifeOS Signature calculation.
 *
 * @param inputs The primary [LifeOSSignatureInputs] entity data.
 * @param vaults The current [VaultsSnapshot] evaluating static document structures.
 * @param capacity The current [CapacitySnapshot] evaluating systemic load.
 * @param playbooks The current [PlaybookSnapshot] evaluating behavioral playbooks.
 * @param currentMode The currently active focus Mode, if any.
 */
internal data class LifeOSSignatureContext(
    val inputs: LifeOSSignatureInputs,
    val vaults: VaultsSnapshot,
    val capacity: CapacitySnapshot,
    val playbooks: PlaybookSnapshot,
    val currentMode: ModeEntity?,
)

/**
 * An internal data class bundling the inputs required to assess "Second Brain" knowledge capture usage.
 *
 * @param nodes The complete list of active nodes in the system.
 * @param relations The complete list of explicit bidirectional links between nodes.
 * @param dashboard The current [DashboardUIState] reflecting immediately visible items.
 * @param areaHealth The current [AreaHealthSnapshot] detailing structural health.
 * @param openLoops The current [OpenLoopsSnapshot] detailing capture behavior.
 */
internal data class LifeOSSecondBrainInputs(
    val nodes: List<NodeWithPin>,
    val relations: List<RelationEntity>,
    val dashboard: DashboardUIState,
    val areaHealth: AreaHealthSnapshot,
    val openLoops: OpenLoopsSnapshot,
)

/**
 * An internal data class providing extended contextual layers to assess LifeOS vs Second Brain posture.
 *
 * @param inputs The primary [LifeOSSecondBrainInputs] core entity data.
 * @param maintenance The current [MaintenanceSnapshot] evaluating action-oriented behaviors.
 * @param capacity The current [CapacitySnapshot] evaluating execution pressure.
 * @param protocols The current [TransitionProtocolsSnapshot] evaluating procedural routines.
 * @param playbooks The current [PlaybookSnapshot] evaluating repeatable processes.
 */
internal data class LifeOSSecondBrainContext(
    val inputs: LifeOSSecondBrainInputs,
    val maintenance: MaintenanceSnapshot,
    val capacity: CapacitySnapshot,
    val protocols: TransitionProtocolsSnapshot,
    val playbooks: PlaybookSnapshot,
)

/**
 * An internal data class consolidating disparate snapshots to calculate overall system direction and commitment alignment.
 *
 * @param distinction The [LifeOSSecondBrainSnapshot] assessing execution vs capture.
 * @param signature The [LifeOSSignatureSnapshot] assessing system depth.
 * @param dashboard The [DashboardUIState] tracking immediate execution.
 * @param logistics The [PhysicalLogisticsSnapshot] tracking spatial execution.
 * @param capacity The [CapacitySnapshot] tracking temporal execution bounds.
 */
internal data class CombinedDirectionInputs(
    val distinction: LifeOSSecondBrainSnapshot,
    val signature: LifeOSSignatureSnapshot,
    val dashboard: DashboardUIState,
    val logistics: PhysicalLogisticsSnapshot,
    val capacity: CapacitySnapshot,
)

/**
 * An internal data class consolidating high-level directional snapshots to evaluate fundamental paradigm shifts in usage.
 *
 * @param distinction The [LifeOSSecondBrainSnapshot].
 * @param signature The [LifeOSSignatureSnapshot].
 * @param direction The [CombinedDirectionSnapshot] proving structural alignment.
 * @param dashboard The [DashboardUIState].
 * @param time The [TimeArchitectureSnapshot] proving temporal awareness.
 */
internal data class CoreLifeOSShiftInputs(
    val distinction: LifeOSSecondBrainSnapshot,
    val signature: LifeOSSignatureSnapshot,
    val direction: CombinedDirectionSnapshot,
    val dashboard: DashboardUIState,
    val time: TimeArchitectureSnapshot,
)

/**
 * An internal data class extending the Core LifeOS Shift inputs with necessary subsystem states.
 *
 * @param inputs The core [CoreLifeOSShiftInputs] foundational data.
 * @param areaHealth The [AreaHealthSnapshot] detailing balanced focus across life areas.
 * @param openLoops The [OpenLoopsSnapshot] indicating control over raw inputs.
 * @param maintenance The [MaintenanceSnapshot] proving baseline stability.
 * @param protocols The [TransitionProtocolsSnapshot] proving structured transitions.
 */
internal data class CoreLifeOSShiftContext(
    val inputs: CoreLifeOSShiftInputs,
    val areaHealth: AreaHealthSnapshot,
    val openLoops: OpenLoopsSnapshot,
    val maintenance: MaintenanceSnapshot,
    val protocols: TransitionProtocolsSnapshot,
)

/**
 * An internal data class grouping the most fundamental search and filter criteria.
 *
 * @param query The raw text query string used for partial matching on title or content.
 * @param type The distinct node type to filter by (e.g., "task", "note").
 * @param status A comma-separated string of node statuses (e.g., "active,on_hold").
 * @param projectId The explicit numeric ID of a Project the node must belong to.
 * @param areaId The explicit numeric ID of an Area the node must belong to.
 */
internal data class SearchPrimaryFilters(
    val query: String,
    val type: String?,
    val status: String?,
    val projectId: Long?,
    val areaId: Long?,
)

/**
 * An internal data class grouping secondary, attribute-based search and filter criteria.
 *
 * @param linkedToId The explicit ID of another node that the target node must share a relation with.
 * @param maxMins The maximum estimated minutes allowed for the node.
 * @param energy The integer level of energy required for the node (1-100).
 * @param friction The string representing the psychological friction (e.g., "low", "high").
 * @param locationContext The specific physical or digital location string required.
 */
internal data class SearchSecondaryFilters(
    val linkedToId: Long?,
    val maxMins: Int?,
    val energy: Int?,
    val friction: String?,
    val locationContext: String?,
)

/**
 * An internal data class grouping complex or hyper-specific tertiary context filters.
 *
 * @param energyContext The specific energy state required (e.g., "fresh", "drained").
 * @param deviceContext The specific device required (e.g., "laptop", "phone").
 * @param socialContext The social parameter required (e.g., "solo", "pair").
 * @param timeWindowContext The required time block constraint (e.g., "morning", "evening").
 * @param timeHorizon A string describing temporal proximity (e.g., "today", "week").
 */
internal data class SearchTertiaryFilters(
    val energyContext: String?,
    val deviceContext: String?,
    val socialContext: String?,
    val timeWindowContext: String?,
    val timeHorizon: String?,
)

/**
 * A combined state data class consolidating all possible search and filter parameters used in the UI layer.
 *
 * @param query The raw text query string used for partial matching.
 * @param type The distinct node type to filter by.
 * @param status A comma-separated string of node statuses.
 * @param projectId The explicit numeric ID of a Project the node must belong to.
 * @param areaId The explicit numeric ID of an Area the node must belong to.
 * @param linkedToId The explicit ID of another node that the target node must share a relation with.
 * @param maxMins The maximum estimated minutes allowed for the node.
 * @param energy The integer level of energy required for the node (1-100).
 * @param friction The string representing the psychological friction.
 * @param locationContext The specific physical or digital location string required.
 * @param energyContext The specific energy state required.
 * @param deviceContext The specific device required.
 * @param socialContext The social parameter required.
 * @param timeWindowContext The required time block constraint.
 * @param timeHorizon A string describing temporal proximity relative to now.
 */
internal data class SearchFiltersState(
    val query: String = "",
    val type: String? = null,
    val status: String? = null,
    val projectId: Long? = null,
    val areaId: Long? = null,
    val linkedToId: Long? = null,
    val maxMins: Int? = null,
    val energy: Int? = null,
    val friction: String? = null,
    val locationContext: String? = null,
    val energyContext: String? = null,
    val deviceContext: String? = null,
    val socialContext: String? = null,
    val timeWindowContext: String? = null,
    val timeHorizon: String? = null,
)
