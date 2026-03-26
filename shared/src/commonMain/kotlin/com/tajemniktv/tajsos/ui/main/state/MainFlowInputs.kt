/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui

import com.tajemniktv.tajsos.data.ModeEntity
import com.tajemniktv.tajsos.data.NodeEntity
import com.tajemniktv.tajsos.data.NodeWithPin
import com.tajemniktv.tajsos.data.RelationEntity
import com.tajemniktv.tajsos.data.TrackEntryEntity

internal data class CapacityInputs(
    val nodes: List<NodeWithPin>,
    val projects: List<NodeEntity>,
    val areas: List<NodeEntity>,
    val maintenance: MaintenanceSnapshot,
    val openLoops: OpenLoopsSnapshot,
)

internal data class LifeOSSignatureInputs(
    val modes: List<ModeEntity>,
    val areaHealth: AreaHealthSnapshot,
    val openLoops: OpenLoopsSnapshot,
    val maintenance: MaintenanceSnapshot,
    val relationships: RelationshipSnapshot,
)

internal data class LifeOSSignatureContext(
    val inputs: LifeOSSignatureInputs,
    val vaults: VaultsSnapshot,
    val capacity: CapacitySnapshot,
    val playbooks: PlaybookSnapshot,
    val currentMode: ModeEntity?,
)

internal data class LifeOSSecondBrainInputs(
    val nodes: List<NodeWithPin>,
    val relations: List<RelationEntity>,
    val dashboard: DashboardUIState,
    val areaHealth: AreaHealthSnapshot,
    val openLoops: OpenLoopsSnapshot,
)

internal data class LifeOSSecondBrainContext(
    val inputs: LifeOSSecondBrainInputs,
    val maintenance: MaintenanceSnapshot,
    val capacity: CapacitySnapshot,
    val protocols: TransitionProtocolsSnapshot,
    val playbooks: PlaybookSnapshot,
)

internal data class CombinedDirectionInputs(
    val distinction: LifeOSSecondBrainSnapshot,
    val signature: LifeOSSignatureSnapshot,
    val dashboard: DashboardUIState,
    val logistics: PhysicalLogisticsSnapshot,
    val capacity: CapacitySnapshot,
)

internal data class CoreLifeOSShiftInputs(
    val distinction: LifeOSSecondBrainSnapshot,
    val signature: LifeOSSignatureSnapshot,
    val direction: CombinedDirectionSnapshot,
    val dashboard: DashboardUIState,
    val time: TimeArchitectureSnapshot,
)

internal data class CoreLifeOSShiftContext(
    val inputs: CoreLifeOSShiftInputs,
    val areaHealth: AreaHealthSnapshot,
    val openLoops: OpenLoopsSnapshot,
    val maintenance: MaintenanceSnapshot,
    val protocols: TransitionProtocolsSnapshot,
)

internal data class SearchPrimaryFilters(
    val query: String,
    val type: String?,
    val status: String?,
    val projectId: Long?,
    val areaId: Long?,
)

internal data class SearchSecondaryFilters(
    val linkedToId: Long?,
    val maxMins: Int?,
    val energy: Int?,
    val friction: String?,
    val locationContext: String?,
)

internal data class SearchTertiaryFilters(
    val energyContext: String?,
    val deviceContext: String?,
    val socialContext: String?,
    val timeWindowContext: String?,
    val timeHorizon: String?,
)

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
