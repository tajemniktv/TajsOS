package com.tajemniktv.tajsos.data

import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.assertFalse

class LifeObjectModelsEdgeTest {

    @Test
    fun matchesItemFilter_returns_true_for_null_filter() {
        val node = NodeEntity(type = "task", title = "Test")
        assertTrue(node.matchesItemFilter(null))
    }

    @Test
    fun matchesItemFilter_matches_exact_type_for_unrecognized_filter() {
        val node = NodeEntity(type = "custom_type", title = "Test")
        assertTrue(node.matchesItemFilter("custom_type"))
        assertFalse(node.matchesItemFilter("other_type"))
    }
}
