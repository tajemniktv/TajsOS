/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajos.ui.components

import androidx.compose.material3.SliderState
import androidx.compose.runtime.AbstractApplier
import androidx.compose.runtime.Composition
import androidx.compose.runtime.Recomposer
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotSame
import kotlin.test.assertSame

/** Exercises the controlled-state contract without mounting a desktop or Android window. */
class TactileSliderStateTest {
    @Test
    fun externalValueUpdatesReuseTheStateAndMoveTheThumb() = withSliderComposition { render ->
        val initial = render(1f, 0f..5f, 4)
        val updated = render(4f, 0f..5f, 4)

        assertSame(initial, updated)
        assertEquals(4f, updated.value)
    }

    @Test
    fun rangeAndStepChangesReplaceImmutableConfiguration() = withSliderComposition { render ->
        val initial = render(2f, 0f..5f, 4)
        val expanded = render(8f, 0f..10f, 4)
        val continuous = render(7.25f, 0f..10f, 0)

        assertNotSame(initial, expanded)
        assertEquals(0f..10f, expanded.trackRange)
        assertEquals(8f, expanded.value)
        assertNotSame(expanded, continuous)
        assertEquals(0, continuous.steps)
        assertEquals(7.25f, continuous.value)
    }

    @Test
    fun externalValuesRespectTheRangeAndDiscreteTicks() = withSliderComposition { render ->
        assertEquals(5f, render(20f, 0f..5f, 4).value)
        assertEquals(0f, render(-2f, 0f..5f, 4).value)
        assertEquals(3f, render(2.8f, 0f..5f, 4).value)
    }

    private fun withSliderComposition(
        block: ((Float, ClosedFloatingPointRange<Float>, Int) -> SliderState) -> Unit,
    ) {
        val recomposer = Recomposer(EmptyCoroutineContext)
        val composition = Composition(NoUiApplier(), recomposer)
        try {
            block { value, range, steps ->
                lateinit var state: SliderState
                composition.setContent {
                    state = rememberTactileSliderState(value, range, steps)
                }
                state
            }
        } finally {
            composition.dispose()
            recomposer.close()
        }
    }

    private class NoUiApplier : AbstractApplier<Unit>(Unit) {
        override fun insertTopDown(index: Int, instance: Unit) = Unit
        override fun insertBottomUp(index: Int, instance: Unit) = Unit
        override fun remove(index: Int, count: Int) = Unit
        override fun move(from: Int, to: Int, count: Int) = Unit
        override fun onClear() = Unit
    }
}
