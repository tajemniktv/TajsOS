/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajos.data

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MedicationValidationTest {
    @Test fun optionalHourInputDistinguishesMissingAndInvalidValues() {
        listOf("", "  ", "0", "23", " 12 ").forEach { assertTrue(isValidMedicationHour(it), it) }
        listOf("-1", "24", "12:30", "abc", "99999999999999999999").forEach { assertFalse(isValidMedicationHour(it), it) }
    }

    @Test fun requiredSubstanceAndHourBoundariesAreSharedWithPersistence() {
        assertFalse(isValidMedication(" ", null))
        assertTrue(isValidMedication("Synthetic", null))
        assertTrue(isValidMedication("Synthetic", 0))
        assertTrue(isValidMedication("Synthetic", 23))
        assertFalse(isValidMedication("Synthetic", -1))
        assertFalse(isValidMedication("Synthetic", 24))
    }
}
