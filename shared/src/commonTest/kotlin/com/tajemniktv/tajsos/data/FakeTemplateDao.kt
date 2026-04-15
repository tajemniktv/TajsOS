package com.tajemniktv.tajsos.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeTemplateDao : TemplateDao {
    private val storedTemplates = mutableListOf<TemplateEntity>()
    private val templatesFlow = MutableStateFlow<List<TemplateEntity>>(emptyList())

    override fun getAllTemplates(): Flow<List<TemplateEntity>> = templatesFlow

    override suspend fun insertTemplate(template: TemplateEntity) {
        val index = storedTemplates.indexOfFirst { it.id == template.id && it.id != 0L }
        if (index != -1) {
            storedTemplates[index] = template
        } else {
            val newId = if (template.id == 0L) (storedTemplates.size + 1).toLong() else template.id
            storedTemplates.add(template.copy(id = newId))
        }
        templatesFlow.value = storedTemplates.toList()
    }

    override suspend fun insertTemplates(templates: List<TemplateEntity>) {
        for (template in templates) {
            insertTemplate(template)
        }
    }

    override suspend fun updateTemplate(template: TemplateEntity) {
        insertTemplate(template)
    }

    override suspend fun deleteTemplate(template: TemplateEntity) {
        storedTemplates.removeAll { it.id == template.id }
        templatesFlow.value = storedTemplates.toList()
    }
}
