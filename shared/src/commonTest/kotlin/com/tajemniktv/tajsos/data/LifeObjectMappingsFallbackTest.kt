package com.tajemniktv.tajsos.data

import kotlin.test.Test
import kotlin.test.assertNull
import kotlin.test.assertEquals

class LifeObjectMappingsFallbackTest {
    @Test
    fun testSavedViewEntityToDefinition_invalid() {
        // Contains invalid keys for enum mapping
        val entity = SavedViewEntity(
            id = 1,
            name = "View",
            description = "Desc",
            lens = "invalid_lens",
            layout = "invalid_layout",
            rowDimension = "invalid_row",
            columnDimension = "invalid_col",
            measure = "invalid_measure",
            createdAt = 1000L,
            updatedAt = 2000L
        )
        val sourceKinds = listOf(SavedViewSourceKindEntity(viewId = 1, itemKind = "invalid_kind"))
        val filters = listOf(SavedViewFilterEntity(viewId = 1, fieldKey = "invalid_field", operatorKey = "invalid_op", value = "val", valueType = "invalid_type", position = 0))
        val sorts = listOf(SavedViewSortEntity(viewId = 1, fieldKey = "invalid_field", direction = "invalid_dir", position = 0))
        val visibleFields = listOf(SavedViewVisibleFieldEntity(viewId = 1, fieldKey = "invalid_field", position = 0))

        val definition = entity.toDefinition(sourceKinds, filters, sorts, visibleFields)

        // Defaults to OPERATE
        assertEquals(SavedViewLens.OPERATE, definition.lens)
        // Defaults to LIST
        assertEquals(SavedViewLayout.LIST, definition.layout)

        // Invalid source kind gets filtered out
        assertEquals(0, definition.sourceKinds.size)
        // Invalid filter fieldKey gets filtered out
        assertEquals(0, definition.filters.size)
        // Invalid sort fieldKey gets filtered out
        assertEquals(0, definition.sorts.size)
        // Invalid visible field gets filtered out
        assertEquals(0, definition.visibleFields.size)

        // Null fallbacks for dimensions
        assertNull(definition.rowDimension)
        assertNull(definition.columnDimension)
        assertNull(definition.measure)
    }

    @Test
    fun testSavedViewEntityToDefinition_partiallyInvalidFilterAndSort() {
        val entity = SavedViewEntity(id = 1, name = "View", description = "Desc", lens = "operate", layout = "list", rowDimension = "kind", columnDimension = "status", measure = "count", createdAt = 1000L, updatedAt = 2000L)
        val sourceKinds = emptyList<SavedViewSourceKindEntity>()

        val filters = listOf(
            // Invalid operator but valid fieldKey -> should be filtered out
            SavedViewFilterEntity(viewId = 1, fieldKey = "status", operatorKey = "invalid_op", value = "val", valueType = "string", position = 0),
            // Valid operator and fieldKey but invalid valueType -> falls back to STRING
            SavedViewFilterEntity(viewId = 1, fieldKey = "kind", operatorKey = "equals", value = "val", valueType = "invalid_type", position = 1)
        )

        val sorts = listOf(
            // Valid fieldKey but invalid direction -> falls back to ASCENDING
            SavedViewSortEntity(viewId = 1, fieldKey = "title", direction = "invalid_dir", position = 0)
        )

        val definition = entity.toDefinition(sourceKinds, filters, sorts, emptyList())

        assertEquals(1, definition.filters.size)
        assertEquals(SavedViewFieldKey.KIND, definition.filters[0].fieldKey)
        assertEquals(SavedViewValueType.STRING, definition.filters[0].valueType) // fallback

        assertEquals(1, definition.sorts.size)
        assertEquals(SavedViewFieldKey.TITLE, definition.sorts[0].fieldKey)
        assertEquals(SavedViewSortDirection.ASCENDING, definition.sorts[0].direction) // fallback
    }
}
