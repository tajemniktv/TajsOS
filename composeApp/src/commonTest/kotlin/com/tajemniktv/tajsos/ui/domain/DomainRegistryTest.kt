/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.domain

import com.tajemniktv.tajsos.domain.DomainKind
import com.tajemniktv.tajsos.ui.Screen
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class DomainRegistryTest {
    @Test
    fun domainRegistry_containsFourBuiltInDomains() {
        assertEquals(
            setOf(
                DomainKind.FINANCES,
                DomainKind.HEALTH,
                DomainKind.EDUCATION,
                DomainKind.RELATIONSHIPS,
            ),
            DomainRegistry.definitions.map { it.kind }.toSet(),
        )
    }

    @Test
    fun domainRoutes_areStable() {
        assertEquals("finances", DomainRegistry.byKind(DomainKind.FINANCES)?.route)
        assertEquals("health", DomainRegistry.byKind(DomainKind.HEALTH)?.route)
        assertEquals("education", DomainRegistry.byKind(DomainKind.EDUCATION)?.route)
        assertEquals("relationships", DomainRegistry.byKind(DomainKind.RELATIONSHIPS)?.route)
    }

    @Test
    fun areasRemainGeneric_navigationStillUsesAreaDetail() {
        assertEquals("area/{areaId}", Screen.AreaDetail.route)
        assertNotNull(DomainRegistry.byRoute("education"))
    }
}
