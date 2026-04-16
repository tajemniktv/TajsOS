/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeTemplateDao : TemplateDao {
    private val templates = mutableListOf<TemplateEntity>()
    private val templatesFlow = MutableStateFlow<List<TemplateEntity>>(emptyList())

    override fun getAllTemplates(): Flow<List<TemplateEntity>> = templatesFlow

    override suspend fun insertTemplate(template: TemplateEntity) {
        upsertTemplateInternal(template)
        templatesFlow.value = templates.toList()
    }

    override suspend fun insertTemplates(templates: List<TemplateEntity>) {
        for (template in templates) {
            upsertTemplateInternal(template)
        }
        templatesFlow.value = this.templates.toList()
    }

    private fun upsertTemplateInternal(template: TemplateEntity) {
        val toInsert =
            if (template.id == 0L) {
                template.copy(id = (this.templates.size + 1).toLong())
            } else {
                template
            }
        val index = this.templates.indexOfFirst { it.id == toInsert.id }
        if (index != -1) {
            this.templates[index] = toInsert
        } else {
            this.templates.add(toInsert)
        }
    }

    override suspend fun updateTemplate(template: TemplateEntity) {
        val index = templates.indexOfFirst { it.id == template.id }
        if (index != -1) {
            templates[index] = template
            templatesFlow.value = templates.toList()
        }
    }

    override suspend fun deleteTemplate(template: TemplateEntity) {
        templates.removeAll { it.id == template.id }
        templatesFlow.value = templates.toList()
    }
}
