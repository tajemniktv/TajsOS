package com.tajemniktv.tajsos.data

import com.tajemniktv.tajsos.domain.DomainKind
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class LifeObjectMappingsTest {
    @Test
    fun testTaskFacetEntityToModel() {
        val entity = TaskFacetEntity(itemId = 1, state = "active", energyLevel = 3, friction = "easy", nextStep = "Do this", estimatedMinutes = 30, completionNote = "Done", completedAt = 1000L, isRecurring = true, recurringInterval = "daily")
        val model = entity.toModel()
        assertEquals(TaskState.ACTIVE, model.state)
        assertEquals(3, model.energyLevel)
        assertEquals("easy", model.friction)
        assertEquals("Do this", model.nextStep)
        assertEquals(30, model.estimatedMinutes)
        assertEquals("Done", model.completionNote)
        assertEquals(1000L, model.completedAt)
        assertEquals(true, model.isRecurring)
        assertEquals("daily", model.recurringInterval)
    }

    @Test
    fun testTaskFacetEntityToModel_fallbackState() {
        val entity = TaskFacetEntity(itemId = 1, state = "invalid")
        val model = entity.toModel()
        assertEquals(TaskState.ACTIVE, model.state)
    }

    @Test
    fun testNoteFacetEntityToModel() {
        val entity = NoteFacetEntity(itemId = 1, kind = "reference", state = "distilled", sourceTitle = "Book", sourceAuthor = "Author", lastReviewedAt = 1000L)
        val model = entity.toModel()
        assertEquals(NoteKind.REFERENCE, model.kind)
        assertEquals(NoteState.DISTILLED, model.state)
        assertEquals("Book", model.sourceTitle)
        assertEquals("Author", model.sourceAuthor)
        assertEquals(1000L, model.lastReviewedAt)
    }

    @Test
    fun testNoteFacetEntityToModel_fallback() {
        val entity = NoteFacetEntity(itemId = 1, kind = "invalid", state = "invalid")
        val model = entity.toModel()
        assertEquals(NoteKind.GENERAL, model.kind)
        assertEquals(NoteState.ACTIVE, model.state)
    }

    @Test
    fun testProjectFacetEntityToModel() {
        val entity = ProjectFacetEntity(itemId = 1, state = "active", purpose = "Goal", isFrozen = true)
        val model = entity.toModel()
        assertEquals(ProjectState.ACTIVE, model.state)
        assertEquals("Goal", model.purpose)
        assertEquals(true, model.isFrozen)
    }

    @Test
    fun testRecordFacetEntityToModel() {
        val entity = RecordFacetEntity(itemId = 1, kind = "health_log", occurredAt = 1000L)
        val model = entity.toModel()
        assertEquals(RecordKind.HEALTH_LOG, model.kind)
        assertEquals(1000L, model.occurredAt)
    }

    @Test
    fun testAreaFacetEntityToModel() {
        val entity = AreaFacetEntity(itemId = 1, healthStatus = "stable", standardOfCare = "Care", vision = "Vision")
        val model = entity.toModel()
        assertEquals(AreaHealthStatus.STABLE, model.healthStatus)
        assertEquals("Care", model.standardOfCare)
        assertEquals("Vision", model.vision)
    }

    @Test
    fun testScheduleEntryEntityToModel() {
        val entity = ScheduleEntryEntity(id = 1, itemId = 2, kind = "event", scheduledAt = 1000L, localDateEpochDay = 200, timezoneId = "UTC", isAllDay = true, endAt = 2000L, recurrenceRule = "FREQ=DAILY", note = "Note", completedAt = 3000L)
        val model = entity.toModel()
        assertEquals(1L, model.id)
        assertEquals(2L, model.itemId)
        assertEquals(ScheduleEntryKind.EVENT, model.kind)
        assertEquals(1000L, model.scheduledAt)
        assertEquals(200, model.localDateEpochDay)
        assertEquals("UTC", model.timezoneId)
        assertEquals(true, model.isAllDay)
        assertEquals(2000L, model.endAt)
        assertEquals("FREQ=DAILY", model.recurrenceRule)
        assertEquals("Note", model.note)
        assertEquals(3000L, model.completedAt)
    }

    @Test
    fun testItemDomainEntityToModel() {
        val entity = ItemDomainEntity(itemId = 1, domainKey = "FINANCES", isPrimary = true, assignedAt = 1000L)
        val model = entity.toModel()
        assertEquals(DomainKind.FINANCES, model?.domain)
        assertEquals(true, model?.isPrimary)
        assertEquals(1000L, model?.assignedAt)

        val invalidEntity = ItemDomainEntity(itemId = 1, domainKey = "INVALID", isPrimary = true, assignedAt = 1000L)
        assertNull(invalidEntity.toModel())
    }

    @Test
    fun testRichContentDocumentEntityToModel() {
        val entity = RichContentDocumentEntity(itemId = 1, format = "markdown", body = "{}", structuredContentJson = "{\"a\":1}", schemaVersion = 2, updatedAt = 1000L)
        val model = entity.toModel()
        assertEquals(1L, model.itemId)
        assertEquals(RichContentFormat.MARKDOWN, model.format)
        assertEquals("{}", model.body)
        assertEquals("{\"a\":1}", model.structuredContentJson)
        assertEquals(2, model.schemaVersion)
        assertEquals(1000L, model.updatedAt)
    }

    @Test
    fun testSavedViewEntityToDefinition() {
        val entity = SavedViewEntity(id = 1, name = "View", description = "Desc", lens = "operate", layout = "list", rowDimension = "kind", columnDimension = "status", measure = "count", createdAt = 1000L, updatedAt = 2000L)
        val sourceKinds = listOf(SavedViewSourceKindEntity(viewId = 1, itemKind = "task"))
        val filters = listOf(SavedViewFilterEntity(viewId = 1, fieldKey = "kind", operatorKey = "equals", value = "task", valueType = "string", position = 0))
        val sorts = listOf(SavedViewSortEntity(viewId = 1, fieldKey = "updated_at", direction = "desc", position = 0))
        val visibleFields = listOf(SavedViewVisibleFieldEntity(viewId = 1, fieldKey = "title", position = 0))

        val definition = entity.toDefinition(sourceKinds, filters, sorts, visibleFields)
        assertEquals(1L, definition.id)
        assertEquals("View", definition.name)
        assertEquals("Desc", definition.description)
        assertEquals(SavedViewLens.OPERATE, definition.lens)
        assertEquals(SavedViewLayout.LIST, definition.layout)
        assertEquals(setOf(ItemKind.TASK), definition.sourceKinds)

        assertEquals(1, definition.filters.size)
        assertEquals(SavedViewFieldKey.KIND, definition.filters[0].fieldKey)
        assertEquals(SavedViewFilterOperator.EQUALS, definition.filters[0].operator)
        assertEquals("task", definition.filters[0].value)
        assertEquals(SavedViewValueType.STRING, definition.filters[0].valueType)

        assertEquals(1, definition.sorts.size)
        assertEquals(SavedViewFieldKey.UPDATED_AT, definition.sorts[0].fieldKey)
        assertEquals(SavedViewSortDirection.DESCENDING, definition.sorts[0].direction)

        assertEquals(1, definition.visibleFields.size)
        assertEquals(SavedViewFieldKey.TITLE, definition.visibleFields[0])

        assertEquals(SavedViewFieldKey.KIND, definition.rowDimension)
        assertEquals(SavedViewFieldKey.STATUS, definition.columnDimension)
        assertEquals(SavedViewMeasure.COUNT, definition.measure)
        assertEquals(1000L, definition.createdAt)
        assertEquals(2000L, definition.updatedAt)
    }
}
