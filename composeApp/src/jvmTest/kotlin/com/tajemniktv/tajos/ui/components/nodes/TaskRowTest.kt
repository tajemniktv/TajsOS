/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */
package com.tajemniktv.tajos.ui.components.nodes

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asSkiaBitmap
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.unit.dp
import com.tajemniktv.tajos.data.NodeEntity
import com.tajemniktv.tajos.ui.theme.TajsOSTheme
import org.junit.Rule
import org.junit.Test
import org.jetbrains.skia.Image
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TaskRowTest {
    @get:Rule val rule = createComposeRule()

    @Test
    fun rowSupportsKeyboardAndMouseAndChildActionsDoNotOpenIt() {
        var opened = 0
        var completed = 0
        rule.setContent {
            TajsOSTheme {
                Box(Modifier.width(900.dp)) {
                    TaskRow(NodeEntity(title = "Row task", type = "task"), "Project • Area", { opened++ }, Modifier.testTag("task-row")) {
                        OutlinedButton(onClick = { completed++ }) { Text("Complete test task") }
                    }
                }
            }
        }
        rule.onNodeWithText("Complete test task").performClick()
        rule.runOnIdle { assertEquals(1, completed); assertEquals(0, opened) }
        val row = rule.onNode(hasText("Row task") and hasClickAction())
        row.performMouseInput { click() }
        rule.runOnIdle { assertEquals(1, opened) }
        row.performSemanticsAction(SemanticsActions.RequestFocus) { it() }
        row.assertIsFocused().performKeyInput { pressKey(Key.Enter) }
        rule.runOnIdle { assertEquals(2, opened) }
        row.performMouseInput { click(button = MouseButton.Tertiary) }
        rule.runOnIdle { assertEquals(3, opened) }
        row.performMouseInput { click(button = MouseButton.Secondary) }
        rule.onNodeWithText("Open").assertIsDisplayed().performClick()
        rule.runOnIdle { assertEquals(4, opened) }
    }

    @Test
    fun narrowRowKeepsTitleAndAllActionsInsideItsBounds() {
        rule.setContent {
            TajsOSTheme {
                Box(Modifier.width(360.dp)) {
                    TaskRow(NodeEntity(title = "Phone task", type = "task"), "Project • Area", {}, Modifier.testTag("task-row")) {
                        for (label in listOf("Start Focus", "Open", "Remove from Today", "Done")) {
                            OutlinedButton(onClick = {}) { Text(label) }
                        }
                    }
                }
            }
        }
        val bounds = rule.onNodeWithTag("task-row").fetchSemanticsNode().boundsInRoot
        for (label in listOf("Phone task", "Start Focus", "Open", "Remove from Today", "Done")) {
            val nodeBounds = rule.onNodeWithText(label).assertIsDisplayed().fetchSemanticsNode().boundsInRoot
            assertTrue(nodeBounds.width > 0, label)
            assertTrue(nodeBounds.left >= bounds.left && nodeBounds.right <= bounds.right, label)
        }
        val screenshot = File("build/reports/task-row/narrow.png")
        screenshot.parentFile.mkdirs()
        Image.makeFromBitmap(rule.onRoot().captureToImage().asSkiaBitmap()).use { image ->
            image.encodeToData()!!.use { data -> screenshot.writeBytes(data.bytes) }
        }
    }
}
