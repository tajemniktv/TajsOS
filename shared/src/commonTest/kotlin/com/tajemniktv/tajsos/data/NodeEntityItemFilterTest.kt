package com.tajemniktv.tajsos.data

import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.assertFalse

class NodeEntityItemFilterTest {
    @Test
    fun testMatchesItemFilter_nullFilter() {
        val node = NodeEntity(id = 1, title = "test", type = "task")
        assertTrue(node.matchesItemFilter(null))
    }

    @Test
    fun testMatchesItemFilter_task() {
        val node = NodeEntity(id = 1, title = "test", type = "task")
        val nodeOpenLoop = NodeEntity(id = 2, title = "test", type = "open_loop")
        val nodeNote = NodeEntity(id = 3, title = "test", type = "note")

        assertTrue(node.matchesItemFilter("task"))
        assertTrue(nodeOpenLoop.matchesItemFilter("task"))
        assertFalse(nodeNote.matchesItemFilter("task"))
    }

    @Test
    fun testMatchesItemFilter_note() {
        val nodeNote = NodeEntity(id = 1, title = "test", type = "note")
        val nodeIdea = NodeEntity(id = 2, title = "test", type = "idea")
        val nodeTask = NodeEntity(id = 3, title = "test", type = "task")

        assertTrue(nodeNote.matchesItemFilter("note"))
        assertTrue(nodeIdea.matchesItemFilter("note"))
        assertFalse(nodeTask.matchesItemFilter("note"))
    }

    @Test
    fun testMatchesItemFilter_record() {
        val nodeRecord = NodeEntity(id = 1, title = "test", type = "record")
        val nodeTask = NodeEntity(id = 2, title = "test", type = "task")

        assertTrue(nodeRecord.matchesItemFilter("record"))
        assertFalse(nodeTask.matchesItemFilter("record"))
    }

    @Test
    fun testMatchesItemFilter_project() {
        val nodeProject = NodeEntity(id = 1, title = "test", type = "project")
        val nodeTask = NodeEntity(id = 2, title = "test", type = "task")

        assertTrue(nodeProject.matchesItemFilter("project"))
        assertFalse(nodeTask.matchesItemFilter("project"))
    }

    @Test
    fun testMatchesItemFilter_area() {
        val nodeArea = NodeEntity(id = 1, title = "test", type = "area")
        val nodeTask = NodeEntity(id = 2, title = "test", type = "task")

        assertTrue(nodeArea.matchesItemFilter("area"))
        assertFalse(nodeTask.matchesItemFilter("area"))
    }

    @Test
    fun testMatchesItemFilter_customType() {
        val nodeCustom = NodeEntity(id = 1, title = "test", type = "custom_type")
        val nodeTask = NodeEntity(id = 2, title = "test", type = "task")

        assertTrue(nodeCustom.matchesItemFilter("custom_type"))
        assertFalse(nodeTask.matchesItemFilter("custom_type"))
        assertFalse(nodeCustom.matchesItemFilter("other_type"))
    }
}
