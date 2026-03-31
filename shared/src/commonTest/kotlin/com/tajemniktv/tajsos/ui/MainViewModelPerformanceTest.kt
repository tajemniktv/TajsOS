/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui

import com.tajemniktv.tajsos.data.NodeEntity
import com.tajemniktv.tajsos.data.NodeWithPin
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.time.measureTime

@Suppress("ReplacePrintlnWithLogging")
class MainViewModelPerformanceTest {
    data class NodeCategorization(
        val inbox: List<NodeWithPin> = emptyList(),
        val archived: List<NodeWithPin> = emptyList(),
        val reminders: List<NodeEntity> = emptyList(),
    )

    @Test
    fun benchmarkNodesFiltering(): Unit =
        runBlocking {
            val count = 200000
            val nodes =
                (1..count).map { i ->
                    NodeWithPin(
                        node =
                            NodeEntity(
                                id = i.toLong(),
                                title = "Node $i",
                                type =
                                    if (i % 5 == 0) {
                                        "project"
                                    } else if (i % 7 == 0) {
                                        "area"
                                    } else {
                                        "task"
                                    },
                                status = if (i % 10 == 0) "archived" else "active",
                                inboxState = i % 2 == 0,
                                reminderAt = if (i % 15 == 0) 1000L else null, // A past timestamp
                            ),
                        pin = null,
                    )
                }

            val allNodesFlow = MutableStateFlow(nodes)
            val scope = CoroutineScope(Dispatchers.Unconfined)

            // Simulate ViewModel's current logic
            val timeCurrent =
                measureTime {
                    val inboxNodes =
                        allNodesFlow
                            .map { list ->
                                list.filter {
                                    it.node.inboxState &&
                                        it.node.status != "archived" &&
                                        it.node.type != "project" &&
                                        it.node.type != "area"
                                }
                            }.stateIn(scope, SharingStarted.Eagerly, emptyList())

                    val archivedNodes =
                        allNodesFlow
                            .map { list ->
                                list.filter { it.node.status == "archived" }
                            }.stateIn(scope, SharingStarted.Eagerly, emptyList())

                    val activeReminders =
                        allNodesFlow
                            .map { list ->
                                val now =
                                    kotlin.time.Clock.System
                                        .now()
                                        .toEpochMilliseconds()
                                list.map { it.node }.filter {
                                    it.reminderAt != null && it.reminderAt <= now && it.status == "active"
                                }
                            }.stateIn(scope, SharingStarted.Eagerly, emptyList())

                    val inb = inboxNodes.value
                    val arc = archivedNodes.value
                    val act = activeReminders.value
                    println("Baseline -> Inbox: ${inb.size}, Archived: ${arc.size}, ActiveReminders: ${act.size}")
                }

            // Simulate ViewModel's new logic
            val timeNew =
                measureTime {
                    val categorizedNodes =
                        allNodesFlow
                            .map { list ->
                                val now =
                                    kotlin.time.Clock.System
                                        .now()
                                        .toEpochMilliseconds()
                                val inbox = mutableListOf<NodeWithPin>()
                                val archived = mutableListOf<NodeWithPin>()
                                val reminders = mutableListOf<NodeEntity>()

                                for (item in list) {
                                    val node = item.node

                                    if (node.status == "archived") {
                                        archived.add(item)
                                    }

                                    if (node.inboxState && node.status != "archived" && node.type != "project" && node.type != "area") {
                                        inbox.add(item)
                                    }

                                    if (node.reminderAt != null && node.reminderAt <= now && node.status == "active") {
                                        reminders.add(node)
                                    }
                                }
                                NodeCategorization(inbox, archived, reminders)
                            }.stateIn(
                                scope,
                                SharingStarted.Eagerly,
                                NodeCategorization(),
                            )

                    val inboxNodes2 =
                        categorizedNodes
                            .map { it.inbox }
                            .stateIn(scope, SharingStarted.Eagerly, emptyList())
                    val archivedNodes2 =
                        categorizedNodes
                            .map { it.archived }
                            .stateIn(scope, SharingStarted.Eagerly, emptyList())
                    val activeReminders2 =
                        categorizedNodes
                            .map { it.reminders }
                            .stateIn(scope, SharingStarted.Eagerly, emptyList())

                    val inb = inboxNodes2.value
                    val arc = archivedNodes2.value
                    val act = activeReminders2.value
                    println("Optimized -> Inbox: ${inb.size}, Archived: ${arc.size}, ActiveReminders: ${act.size}")
                }

            println("Baseline time: ${timeCurrent.inWholeMilliseconds} ms")
            println("Optimized time: ${timeNew.inWholeMilliseconds} ms")

            kotlin.test.assertTrue(timeNew.inWholeMilliseconds >= 0)
            kotlin.test.assertTrue(timeCurrent.inWholeMilliseconds >= 0)
        }
}
