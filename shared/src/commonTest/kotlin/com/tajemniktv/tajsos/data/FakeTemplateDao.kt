package com.tajemniktv.tajsos.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeTemplateDao : TemplateDao {
    private val storedTemplates = mutableListOf<TemplateEntity>()
    private val templatesFlow = MutableStateFlow<List<TemplateEntity>>(emptyList())

    override fun getAllTemplates(): Flow<List<TemplateEntity>> = templatesFlow

    override suspend fun insertTemplate(template: TemplateEntity) {
        val newId = (storedTemplates.size + 1).toLong()
        val newTemplate = template.copy(id = newId)
        storedTemplates.add(newTemplate)
        templatesFlow.value = storedTemplates.toList()
    }

    override suspend fun insertTemplates(templates: List<TemplateEntity>) {
        templates.forEach { insertTemplate(it) }
    }

    override suspend fun updateTemplate(template: TemplateEntity) {
        val index = storedTemplates.indexOfFirst { it.id == template.id }
        if (index != -1) {
            storedTemplates[index] = template
            templatesFlow.value = storedTemplates.toList()
        }
    }

    override suspend fun deleteTemplate(template: TemplateEntity) {
        storedTemplates.removeAll { it.id == template.id }
        templatesFlow.value = storedTemplates.toList()
    }
}
