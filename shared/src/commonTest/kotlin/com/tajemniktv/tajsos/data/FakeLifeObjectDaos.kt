/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeInboxEntryDao : InboxEntryDao {
    private val entries = mutableListOf<InboxEntryEntity>()
    private val entriesFlow = MutableStateFlow<List<InboxEntryEntity>>(emptyList())

    override fun getAllInboxEntries(): Flow<List<InboxEntryEntity>> = entriesFlow

    override fun getActiveInboxEntries(): Flow<List<InboxEntryEntity>> =
        entriesFlow.map { list ->
            list.filter { it.processedAt == null && it.dismissedAt == null }
        }

    override suspend fun getInboxEntryById(id: Long): InboxEntryEntity? = entries.firstOrNull { it.id == id }

    override suspend fun insertInboxEntry(entry: InboxEntryEntity): Long {
        val id = (entries.size + 1).toLong()
        entries += entry.copy(id = id)
        entriesFlow.value = entries.toList()
        return id
    }

    override suspend fun updateInboxEntry(entry: InboxEntryEntity) {
        val index = entries.indexOfFirst { it.id == entry.id }
        if (index >= 0) {
            entries[index] = entry
            entriesFlow.value = entries.toList()
        }
    }
}

class FakeTaskFacetDao : TaskFacetDao {
    private val facets = mutableListOf<TaskFacetEntity>()
    private val facetsFlow = MutableStateFlow<List<TaskFacetEntity>>(emptyList())

    override fun getAllTaskFacets(): Flow<List<TaskFacetEntity>> = facetsFlow

    override suspend fun getTaskFacetByItemId(itemId: Long): TaskFacetEntity? =
        facets.firstOrNull { it.itemId == itemId }

    override fun observeTaskFacet(itemId: Long): Flow<TaskFacetEntity?> =
        facetsFlow.map { list -> list.firstOrNull { it.itemId == itemId } }

    override suspend fun upsertTaskFacet(facet: TaskFacetEntity) {
        val index = facets.indexOfFirst { it.itemId == facet.itemId }
        if (index >= 0) {
            facets[index] = facet
        } else {
            facets += facet
        }
        facetsFlow.value = facets.toList()
    }

    override suspend fun deleteTaskFacetForItem(itemId: Long) {
        facets.removeAll { it.itemId == itemId }
        facetsFlow.value = facets.toList()
    }
}

class FakeNoteFacetDao : NoteFacetDao {
    private val facets = mutableListOf<NoteFacetEntity>()
    private val facetsFlow = MutableStateFlow<List<NoteFacetEntity>>(emptyList())

    override fun getAllNoteFacets(): Flow<List<NoteFacetEntity>> = facetsFlow

    override suspend fun getNoteFacetByItemId(itemId: Long): NoteFacetEntity? =
        facets.firstOrNull { it.itemId == itemId }

    override fun observeNoteFacet(itemId: Long): Flow<NoteFacetEntity?> =
        facetsFlow.map { list -> list.firstOrNull { it.itemId == itemId } }

    override suspend fun upsertNoteFacet(facet: NoteFacetEntity) {
        val index = facets.indexOfFirst { it.itemId == facet.itemId }
        if (index >= 0) {
            facets[index] = facet
        } else {
            facets += facet
        }
        facetsFlow.value = facets.toList()
    }

    override suspend fun deleteNoteFacetForItem(itemId: Long) {
        facets.removeAll { it.itemId == itemId }
        facetsFlow.value = facets.toList()
    }
}

class FakeProjectFacetDao : ProjectFacetDao {
    private val facets = mutableListOf<ProjectFacetEntity>()
    private val facetsFlow = MutableStateFlow<List<ProjectFacetEntity>>(emptyList())

    override fun getAllProjectFacets(): Flow<List<ProjectFacetEntity>> = facetsFlow

    override suspend fun getProjectFacetByItemId(itemId: Long): ProjectFacetEntity? =
        facets.firstOrNull { it.itemId == itemId }

    override fun observeProjectFacet(itemId: Long): Flow<ProjectFacetEntity?> =
        facetsFlow.map { list -> list.firstOrNull { it.itemId == itemId } }

    override suspend fun upsertProjectFacet(facet: ProjectFacetEntity) {
        val index = facets.indexOfFirst { it.itemId == facet.itemId }
        if (index >= 0) {
            facets[index] = facet
        } else {
            facets += facet
        }
        facetsFlow.value = facets.toList()
    }

    override suspend fun deleteProjectFacetForItem(itemId: Long) {
        facets.removeAll { it.itemId == itemId }
        facetsFlow.value = facets.toList()
    }
}

class FakeAreaFacetDao : AreaFacetDao {
    private val facets = mutableListOf<AreaFacetEntity>()
    private val facetsFlow = MutableStateFlow<List<AreaFacetEntity>>(emptyList())

    override fun getAllAreaFacets(): Flow<List<AreaFacetEntity>> = facetsFlow

    override suspend fun getAreaFacetByItemId(itemId: Long): AreaFacetEntity? =
        facets.firstOrNull { it.itemId == itemId }

    override fun observeAreaFacet(itemId: Long): Flow<AreaFacetEntity?> =
        facetsFlow.map { list -> list.firstOrNull { it.itemId == itemId } }

    override suspend fun upsertAreaFacet(facet: AreaFacetEntity) {
        val index = facets.indexOfFirst { it.itemId == facet.itemId }
        if (index >= 0) {
            facets[index] = facet
        } else {
            facets += facet
        }
        facetsFlow.value = facets.toList()
    }

    override suspend fun deleteAreaFacetForItem(itemId: Long) {
        facets.removeAll { it.itemId == itemId }
        facetsFlow.value = facets.toList()
    }
}

class FakeRecordFacetDao : RecordFacetDao {
    private val facets = mutableListOf<RecordFacetEntity>()
    private val facetsFlow = MutableStateFlow<List<RecordFacetEntity>>(emptyList())

    override fun getAllRecordFacets(): Flow<List<RecordFacetEntity>> = facetsFlow

    override suspend fun getRecordFacetByItemId(itemId: Long): RecordFacetEntity? =
        facets.firstOrNull { it.itemId == itemId }

    override fun observeRecordFacet(itemId: Long): Flow<RecordFacetEntity?> =
        facetsFlow.map { list -> list.firstOrNull { it.itemId == itemId } }

    override suspend fun upsertRecordFacet(facet: RecordFacetEntity) {
        val index = facets.indexOfFirst { it.itemId == facet.itemId }
        if (index >= 0) {
            facets[index] = facet
        } else {
            facets += facet
        }
        facetsFlow.value = facets.toList()
    }

    override suspend fun deleteRecordFacetForItem(itemId: Long) {
        facets.removeAll { it.itemId == itemId }
        facetsFlow.value = facets.toList()
    }
}

class FakeItemDomainDao : ItemDomainDao {
    private val domains = mutableListOf<ItemDomainEntity>()
    private val domainsFlow = MutableStateFlow<List<ItemDomainEntity>>(emptyList())

    override fun getAllItemDomains(): Flow<List<ItemDomainEntity>> = domainsFlow

    override fun getDomainsForItem(itemId: Long): Flow<List<ItemDomainEntity>> =
        domainsFlow.map { list -> list.filter { it.itemId == itemId } }

    override suspend fun upsertDomains(domains: List<ItemDomainEntity>) {
        val updated = this.domains.toMutableList()
        domains.forEach { domain ->
            val index = updated.indexOfFirst {
                it.itemId == domain.itemId && it.domainKey == domain.domainKey
            }
            if (index >= 0) {
                updated[index] = domain
            } else {
                updated += domain
            }
        }
        this.domains.clear()
        this.domains.addAll(updated)
        domainsFlow.value = this.domains.toList()
    }

    override suspend fun upsertDomain(domain: ItemDomainEntity) {
        val index =
            domains.indexOfFirst { it.itemId == domain.itemId && it.domainKey == domain.domainKey }
        if (index >= 0) {
            domains[index] = domain
        } else {
            domains += domain
        }
        domainsFlow.value = domains.toList()
    }

    override suspend fun deleteDomain(
        itemId: Long,
        domainKey: String,
    ) {
        domains.removeAll { it.itemId == itemId && it.domainKey == domainKey }
        domainsFlow.value = domains.toList()
    }

    override suspend fun deleteDomainsForItem(itemId: Long) {
        domains.removeAll { it.itemId == itemId }
        domainsFlow.value = domains.toList()
    }

    override suspend fun clearPrimaryFlag(itemId: Long) {
        for (i in domains.indices) { val domain = domains[i];
            domains[i] = if (domain.itemId == itemId) domain.copy(isPrimary = false) else domain
        }
        domainsFlow.value = domains.toList()
    }
}

class FakeRichContentDocumentDao : RichContentDocumentDao {
    private val documents = mutableListOf<RichContentDocumentEntity>()
    private val documentsFlow = MutableStateFlow<List<RichContentDocumentEntity>>(emptyList())

    override fun getAllDocuments(): Flow<List<RichContentDocumentEntity>> = documentsFlow

    override fun observeDocumentForItem(itemId: Long): Flow<RichContentDocumentEntity?> =
        documentsFlow.map { list -> list.firstOrNull { it.itemId == itemId } }

    override suspend fun getDocumentForItem(itemId: Long): RichContentDocumentEntity? =
        documents.firstOrNull { it.itemId == itemId }

    override suspend fun upsertDocument(document: RichContentDocumentEntity) {
        val index = documents.indexOfFirst { it.itemId == document.itemId }
        if (index >= 0) {
            documents[index] = document
        } else {
            documents += document
        }
        documentsFlow.value = documents.toList()
    }

    override suspend fun deleteDocumentForItem(itemId: Long) {
        documents.removeAll { it.itemId == itemId }
        documentsFlow.value = documents.toList()
    }
}

class FakeScheduleEntryDao : ScheduleEntryDao {
    private val entries = mutableListOf<ScheduleEntryEntity>()
    private val entriesFlow = MutableStateFlow<List<ScheduleEntryEntity>>(emptyList())

    override fun getAllScheduleEntries(): Flow<List<ScheduleEntryEntity>> = entriesFlow

    override fun getScheduleEntriesForItem(itemId: Long): Flow<List<ScheduleEntryEntity>> =
        entriesFlow.map { list -> list.filter { it.itemId == itemId }.sortedBy { it.scheduledAt } }

    override fun getOpenScheduleEntriesByKindAndDayRange(
        kind: String,
        fromEpochDay: Int,
        toEpochDay: Int,
    ): Flow<List<ScheduleEntryEntity>> =
        entriesFlow.map { list ->
            list.filter {
                it.kind == kind &&
                    it.completedAt == null &&
                    it.localDateEpochDay != null &&
                    it.localDateEpochDay in fromEpochDay..toEpochDay
            }
        }

    override suspend fun getScheduleEntriesByKind(
        itemId: Long,
        kind: String,
    ): List<ScheduleEntryEntity> =
        entries.filter { it.itemId == itemId && it.kind == kind }.sortedBy { it.scheduledAt }

    override suspend fun deleteScheduleEntriesByKind(
        itemId: Long,
        kind: String,
    ) {
        entries.removeAll { it.itemId == itemId && it.kind == kind }
        entriesFlow.value = entries.toList()
    }

    override suspend fun deleteScheduleEntriesForItem(itemId: Long) {
        entries.removeAll { it.itemId == itemId }
        entriesFlow.value = entries.toList()
    }

    override suspend fun insertScheduleEntry(entry: ScheduleEntryEntity): Long {
        val id = (entries.size + 1).toLong()
        entries += entry.copy(id = id)
        entriesFlow.value = entries.toList()
        return id
    }

    override suspend fun insertScheduleEntries(entries: List<ScheduleEntryEntity>) {
        entries.forEach { insertScheduleEntry(it) }
    }
}

class FakeSavedViewDao : SavedViewDao {
    private val views = mutableListOf<SavedViewEntity>()
    private val sourceKinds = mutableListOf<SavedViewSourceKindEntity>()
    private val filters = mutableListOf<SavedViewFilterEntity>()
    private val sorts = mutableListOf<SavedViewSortEntity>()
    private val visibleFields = mutableListOf<SavedViewVisibleFieldEntity>()

    private val viewsFlow = MutableStateFlow<List<SavedViewEntity>>(emptyList())
    private val sourceKindsFlow = MutableStateFlow<List<SavedViewSourceKindEntity>>(emptyList())
    private val filtersFlow = MutableStateFlow<List<SavedViewFilterEntity>>(emptyList())
    private val sortsFlow = MutableStateFlow<List<SavedViewSortEntity>>(emptyList())
    private val visibleFieldsFlow = MutableStateFlow<List<SavedViewVisibleFieldEntity>>(emptyList())

    override fun getAllSavedViews(): Flow<List<SavedViewEntity>> = viewsFlow

    override suspend fun getSavedViewById(id: Long): SavedViewEntity? = views.firstOrNull { it.id == id }

    override suspend fun insertSavedView(view: SavedViewEntity): Long {
        val id = (views.size + 1).toLong()
        views += view.copy(id = id)
        viewsFlow.value = views.toList()
        return id
    }

    override suspend fun updateSavedView(view: SavedViewEntity) {
        val index = views.indexOfFirst { it.id == view.id }
        if (index >= 0) {
            views[index] = view
            viewsFlow.value = views.toList()
        }
    }

    override suspend fun deleteSavedView(view: SavedViewEntity) {
        views.removeAll { it.id == view.id }
        viewsFlow.value = views.toList()
    }

    override fun getAllSavedViewSourceKinds(): Flow<List<SavedViewSourceKindEntity>> = sourceKindsFlow

    override suspend fun getSourceKindsForView(viewId: Long): List<SavedViewSourceKindEntity> =
        sourceKinds.filter { it.viewId == viewId }

    override suspend fun insertSavedViewSourceKinds(sourceKinds: List<SavedViewSourceKindEntity>) {
        this.sourceKinds += sourceKinds
        sourceKindsFlow.value = this.sourceKinds.toList()
    }

    override suspend fun deleteSourceKindsForView(viewId: Long) {
        sourceKinds.removeAll { it.viewId == viewId }
        sourceKindsFlow.value = sourceKinds.toList()
    }

    override fun getAllSavedViewFilters(): Flow<List<SavedViewFilterEntity>> = filtersFlow

    override suspend fun getFiltersForView(viewId: Long): List<SavedViewFilterEntity> =
        filters.filter { it.viewId == viewId }.sortedBy { it.position }

    override suspend fun insertSavedViewFilters(filters: List<SavedViewFilterEntity>) {
        this.filters += filters
        filtersFlow.value = this.filters.toList()
    }

    override suspend fun deleteFiltersForView(viewId: Long) {
        filters.removeAll { it.viewId == viewId }
        filtersFlow.value = filters.toList()
    }

    override fun getAllSavedViewSorts(): Flow<List<SavedViewSortEntity>> = sortsFlow

    override suspend fun getSortsForView(viewId: Long): List<SavedViewSortEntity> =
        sorts.filter { it.viewId == viewId }.sortedBy { it.position }

    override suspend fun insertSavedViewSorts(sorts: List<SavedViewSortEntity>) {
        this.sorts += sorts
        sortsFlow.value = this.sorts.toList()
    }

    override suspend fun deleteSortsForView(viewId: Long) {
        sorts.removeAll { it.viewId == viewId }
        sortsFlow.value = sorts.toList()
    }

    override fun getAllSavedViewVisibleFields(): Flow<List<SavedViewVisibleFieldEntity>> = visibleFieldsFlow

    override suspend fun getVisibleFieldsForView(viewId: Long): List<SavedViewVisibleFieldEntity> =
        visibleFields.filter { it.viewId == viewId }.sortedBy { it.position }

    override suspend fun insertSavedViewVisibleFields(fields: List<SavedViewVisibleFieldEntity>) {
        visibleFields += fields
        visibleFieldsFlow.value = visibleFields.toList()
    }

    override suspend fun deleteVisibleFieldsForView(viewId: Long) {
        visibleFields.removeAll { it.viewId == viewId }
        visibleFieldsFlow.value = visibleFields.toList()
    }
}
