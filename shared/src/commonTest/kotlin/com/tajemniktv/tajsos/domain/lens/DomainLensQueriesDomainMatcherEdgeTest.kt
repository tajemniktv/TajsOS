package com.tajemniktv.tajsos.domain.lens

import com.tajemniktv.tajsos.data.NodeEntity
import com.tajemniktv.tajsos.data.NodeWithPin
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DomainLensQueriesDomainMatcherEdgeTest {

    private fun createNode(
        title: String = "",
        content: String = "",
        maintenanceType: String? = null,
        noteType: String? = null
    ): NodeWithPin {
        return NodeWithPin(
            node = NodeEntity(
                id = 1L,
                title = title,
                content = content,
                type = "task",
                status = "active",
                maintenanceType = maintenanceType,
                noteType = noteType,
            ),
            pin = null,
            tags = emptyList()
        )
    }

    @Test
    fun healthMatcher_emptyTitleAndContent() {
        val node = createNode(title = "", content = "")
        val result = DomainLensQueries.healthActionItems(listOf(node))
        assertTrue(result.isEmpty())
    }

    @Test
    fun financeMatcher_emptyTitleAndContent() {
        val node = createNode(title = "", content = "")
        val result = DomainLensQueries.financeActionItems(listOf(node))
        assertTrue(result.isEmpty())
    }

    @Test
    fun healthMatcher_blankTitleAndContent() {
        val node = createNode(title = "   ", content = "\n\t")
        val result = DomainLensQueries.healthActionItems(listOf(node))
        assertTrue(result.isEmpty())
    }

    @Test
    fun financeMatcher_blankTitleAndContent() {
        val node = createNode(title = "   ", content = "\n\t")
        val result = DomainLensQueries.financeActionItems(listOf(node))
        assertTrue(result.isEmpty())
    }
}
