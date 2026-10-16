/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajos.data

/** The same persisted contract applies to both medication entry points and non-UI callers. */
fun MedicationEntity.isValid(): Boolean = isValidMedication(substance, takeAtHour)

fun isValidMedication(substance: String, takeAtHour: Int?): Boolean =
    substance.isNotBlank() && (takeAtHour == null || takeAtHour in 0..23)

/** Blank means no scheduled hour; malformed and out-of-range input remain invalid. */
fun isValidMedicationHour(input: String): Boolean = input.isBlank() || input.trim().toIntOrNull()?.let { it in 0..23 } == true
