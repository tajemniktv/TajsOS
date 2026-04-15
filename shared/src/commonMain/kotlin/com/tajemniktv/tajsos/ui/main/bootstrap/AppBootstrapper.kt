/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.main.bootstrap

import com.tajemniktv.tajsos.data.AppRepository
import com.tajemniktv.tajsos.data.AreaHealthStatus
import com.tajemniktv.tajsos.data.ItemKind
import com.tajemniktv.tajsos.data.ModeEntity
import com.tajemniktv.tajsos.data.ModePreferenceEntity
import com.tajemniktv.tajsos.data.NodeEntity
import com.tajemniktv.tajsos.data.NoteKind
import com.tajemniktv.tajsos.data.NoteState
import com.tajemniktv.tajsos.data.PreferencesRepository
import com.tajemniktv.tajsos.data.ProjectState
import com.tajemniktv.tajsos.data.TaskState
import com.tajemniktv.tajsos.data.TemplateEntity
import com.tajemniktv.tajsos.data.UserEntity
import com.tajemniktv.tajsos.data.toNodeStatus
import com.tajemniktv.tajsos.domain.DomainKind
import kotlinx.coroutines.flow.first

/**
 * A utility class responsible for initializing and populating the local database with core operational data
 * during the initial onboarding flow or upon system launch.
 *
 * It generates default entities such as focus modes, academic templates, logistical systems,
 * and base user data if the local storage is detected as entirely empty.
 *
 * @property repository The [AppRepository] used for direct database inserts.
 * @property preferencesRepository The [PreferencesRepository] used to handle default tier access checks.
 */
class AppBootstrapper(
    private val repository: AppRepository,
    private val preferencesRepository: PreferencesRepository,
) {
    /**
     * Executes the comprehensive bootstrap sequence.
     *
     * It unconditionally ensures default pack access, seeds static modes and templates, sets up the user entity,
     * and triggers sample onboarding data generation only if the node list is empty when checked.
     */
    suspend fun bootstrap() {
        preferencesRepository.ensureDefaultPackAccess()
        seedDefaultModes()
        seedStudentTemplates()
        seedLifeLogisticsTemplates()
        seedUserData()
        if (repository.getAllNodes().first().isEmpty()) {
            seedOnboardingData()
        }
    }

    /**
     * Creates the baseline [UserEntity] if it doesn't already exist.
     */
    private suspend fun seedUserData() {
        if (repository.getUser().first() == null) {
            repository.insertUser(UserEntity())
        }
    }

    /**
     * Seeds initial sample data to demonstrate system capabilities and provide an immediate
     * working environment for new users.
     *
     * This includes a default area (TajsOS Development), a core project, sample tasks,
     * reference notes, and examples of various item types like records, maintenance,
     * decisions, and open loops.
     */
    private suspend fun seedOnboardingData() {
        if (repository.getAllNodes().first().isNotEmpty()) return

        seedDefaultModes()

        // 1. Create a primary Area: TajsOS Development
        val devAreaId =
            repository.insertNode(
                NodeEntity(
                    title = "TajsOS Development",
                    content = "The core engineering and design space for TajsOS.",
                    type = ItemKind.AREA.storageKey,
                    icon = "terminal",
                    color = 0xFF2196F3.toInt(),
                    inboxState = false,
                    areaHealthStatus = AreaHealthStatus.STABLE.storageKey,
                ),
            )

        // 2. Create a Project: TajsOS Core
        val coreProjectId =
            repository.insertNode(
                NodeEntity(
                    title = "TajsOS Core",
                    content = "Developing the foundation and life-object model.",
                    type = ItemKind.PROJECT.storageKey,
                    areaId = devAreaId,
                    inboxState = false,
                    projectStatus = ProjectState.ACTIVE.storageKey,
                    projectWhy = "Build a robust and extensible personal OS foundation.",
                ),
            )
        // Assign EDUCATION domain as a proxy for Development/Learning
        repository.assignDomainToItem(coreProjectId, DomainKind.EDUCATION, true)

        // 3. Create a Task: Refactor AppBootstrapper
        repository.insertNode(
            NodeEntity(
                title = "Refactor AppBootstrapper",
                content = "Clean up the initial seeding logic and add real-life examples.",
                type = ItemKind.TASK.storageKey,
                projectId = coreProjectId,
                areaId = devAreaId,
                status = TaskState.ACTIVE.toNodeStatus(),
                energyLevel = 2, // Medium
                inboxState = false,
                nextSmallestStep = "Define TajsOS development example data",
            ),
        )

        // 4. Create a Note: TajsOS Architecture
        repository.insertNode(
            NodeEntity(
                title = "TajsOS Architecture",
                content = "KMP + Compose Multiplatform with shared business logic and Room/DataStore persistence.",
                type = ItemKind.NOTE.storageKey,
                projectId = coreProjectId,
                areaId = devAreaId,
                inboxState = false,
                isPinned = true,
                noteType = NoteKind.REFERENCE.storageKey,
                noteState = NoteState.ACTIVE.storageKey,
            ),
        )

        // 5. Create a Record: Build 1.2.0 Released
        repository.insertNode(
            NodeEntity(
                title = "Build 1.2.0 Released",
                content = "Successfully deployed the latest version with enhanced onboarding.",
                type = ItemKind.RECORD.storageKey,
                projectId = coreProjectId,
                areaId = devAreaId,
                inboxState = false,
            ),
        )

        // 6. Create an Inbox Entry
        repository.captureInboxEntry(
            rawText = "Explore TajsOS features and suggest improvements",
            suggestedKind = ItemKind.TASK,
            homeAreaId = devAreaId,
            activeProjectId = coreProjectId,
        )

        // 7. Add a maintenance item
        repository.insertNode(
            NodeEntity(
                title = "Monthly server backup",
                type = "maintenance",
                maintenanceType = "backup",
                areaId = devAreaId,
                inboxState = false,
            ),
        )

        // 8. Add a decision item
        repository.insertNode(
            NodeEntity(
                title = "Choose between Hilt and Koin",
                type = "decision",
                decisionStatus = "pending",
                areaId = devAreaId,
                inboxState = false,
            ),
        )

        // 9. Add an open loop item
        repository.insertNode(
            NodeEntity(
                title = "Reply to research email",
                type = "open_loop",
                openLoopType = "reply_needed",
                areaId = devAreaId,
                inboxState = false,
            ),
        )
    }

    /**
     * Instantiates the core academic templates within the system (e.g., Class Lecture, Reading Summary).
     */
    private suspend fun seedStudentTemplates() {
        val existingNames =
            repository
                .getAllTemplates()
                .first()
                .map { it.name.trim().lowercase() }
                .toSet()
        val templates =
            listOf(
                TemplateEntity(
                    name = "Lecture Note Template",
                    nodeType = "note",
                    defaultTitle = "Lecture - [Course] - [Topic]",
                    defaultContent =
                        """
                        ## Key ideas
                        - 

                        ## Definitions
                        - 

                        ## Questions
                        - 

                        ## Next actions
                        - 
                        """.trimIndent(),
                ),
                TemplateEntity(
                    name = "Reading Note Template",
                    nodeType = "note",
                    defaultTitle = "Reading - [Source] - [Chapter]",
                    defaultContent =
                        """
                        ## Source
                        - Author:
                        - Year:
                        - Link:

                        ## Main argument
                        - 

                        ## Evidence and methods
                        - 

                        ## Quotes
                        - 

                        ## Personal takeaways
                        - 
                        """.trimIndent(),
                ),
                TemplateEntity(
                    name = "Paper Summary Template",
                    nodeType = "note",
                    defaultTitle = "Paper Summary - [Title]",
                    defaultContent =
                        """
                        ## Citation
                        - 

                        ## Research question
                        - 

                        ## Method
                        - 

                        ## Findings
                        - 

                        ## Limitations
                        - 

                        ## Relevance to exam
                        - 
                        """.trimIndent(),
                ),
            )

        val toInsert = templates.filter { it.name.trim().lowercase() !in existingNames }
        if (toInsert.isNotEmpty()) {
            repository.insertTemplates(toInsert)
        }
    }


    private data class ModeDefinition(val mode: ModeEntity, val prefs: ModePreferenceEntity)

    /**
     * Identifies if default system modes exist (e.g., Work, Recovery, Chaos), and inserts them
     * alongside their associated [ModePreferenceEntity] configurations if they are missing.
     */
    private suspend fun seedDefaultModes() {
        val existingModes = repository.getAllModes().first()
        val existingKeys = existingModes.map { it.key }.toSet()

        val defaultModes = listOf(
            ModeDefinition(
                ModeEntity(
                    key = "COMMAND",
                    name = "Command",
                    description = "Default everyday overview mode. What matters right now?",
                    icon = "dashboard",
                    themeColor = 0xFF3F51B5.toInt(),
                ),
                ModePreferenceEntity(
                    modeId = 0L,
                    dashboardBlocksJson = "[\"today_top_3\", \"resume_context\", \"inbox_count\", \"deadlines\", \"overdue\", \"pinned_note\"]",
                )
            ),
            ModeDefinition(
                ModeEntity(
                    key = "FOCUS",
                    name = "Focus",
                    description = "Narrow the system to one thing. keep attention on this.",
                    icon = "center_focus_strong",
                    sortOrder = 1,
                    themeColor = 0xFFF44336.toInt(),
                ),
                ModePreferenceEntity(
                    modeId = 0L,
                    showInbox = false,
                    showStats = false,
                    dashboardBlocksJson = "[\"current_task\", \"next_step\", \"timer\", \"blockers\", \"linked_resources\"]",
                )
            ),
            ModeDefinition(
                ModeEntity(
                    key = "RECOVERY",
                    name = "Recovery",
                    description = "Support low-capacity functioning. Smallest safe useful thing.",
                    icon = "medical_services",
                    sortOrder = 2,
                    themeColor = 0xFF4CAF50.toInt(),
                ),
                ModePreferenceEntity(
                    modeId = 0L,
                    showInbox = false,
                    showStats = false,
                    dashboardBlocksJson = "[\"basics\", \"easy_wins\", \"urgent_only\", \"recovery_protocol\", \"check_in\"]",
                )
            ),
            ModeDefinition(
                ModeEntity(
                    key = "STUDY",
                    name = "Study",
                    description = "Focus on learning and academic performance.",
                    icon = "school",
                    sortOrder = 3,
                    themeColor = 0xFFFF9800.toInt(),
                ),
                ModePreferenceEntity(
                    modeId = 0L,
                    dashboardBlocksJson = "[\"classes\", \"assignments\", \"deadlines\", \"notes\", \"revision_targets\"]",
                )
            ),
            ModeDefinition(
                ModeEntity(
                    key = "ERRAND",
                    name = "Errand",
                    description = "Out-of-home execution and logistical clustering.",
                    icon = "shopping_cart",
                    sortOrder = 4,
                    themeColor = 0xFF00BCD4.toInt(),
                ),
                ModePreferenceEntity(
                    modeId = 0L,
                    dashboardBlocksJson = "[\"shopping_list\", \"place_based_tasks\", \"errands\", \"what_to_bring\"]",
                )
            ),
            ModeDefinition(
                ModeEntity(
                    key = "ADMIN",
                    name = "Admin",
                    description = "Handle the 'paperwork' of life. Subscriptions, bills, forms.",
                    icon = "gavel",
                    sortOrder = 5,
                    themeColor = 0xFF607D8B.toInt(),
                ),
                ModePreferenceEntity(
                    modeId = 0L,
                    dashboardBlocksJson = "[\"paperwork\", \"bills\", \"renewals\", \"subscriptions\", \"bureaucracy\"]",
                )
            ),
            ModeDefinition(
                ModeEntity(
                    key = "SHUTDOWN",
                    name = "Shutdown",
                    description = "Nightly reset and preparation for tomorrow.",
                    icon = "bedtime",
                    sortOrder = 6,
                    themeColor = 0xFF673AB7.toInt(),
                ),
                ModePreferenceEntity(
                    modeId = 0L,
                    dashboardBlocksJson = "[\"tomorrow_prep\", \"mini_review\", \"dump_leftovers\", \"open_loops_reduction\"]",
                )
            ),
            ModeDefinition(
                ModeEntity(
                    key = "LOW_BATTERY",
                    name = "Low Battery",
                    description = "Minimal survival mode for when you are emotionally or physically drained.",
                    icon = "battery_alert",
                    sortOrder = 7,
                    themeColor = 0xFFE91E63.toInt(),
                ),
                ModePreferenceEntity(
                    modeId = 0L,
                    showInbox = false,
                    dashboardBlocksJson = "[\"survival_basics\", \"tiny_wins\", \"passive_input\", \"comfort_notes\"]",
                )
            ),
            ModeDefinition(
                ModeEntity(
                    key = "ALL",
                    name = "All",
                    description = "Unfiltered access to the entire system. No restrictions.",
                    icon = "all_inclusive",
                    sortOrder = 8,
                    themeColor = 0xFF9E9E9E.toInt(),
                ),
                ModePreferenceEntity(
                    modeId = 0L,
                    dashboardBlocksJson = "[\"today_top_3\", \"search\", \"alerts\", \"focus\", \"insights\", \"knowledge\", \"operational\"]",
                )
            )
        )

        for (def in defaultModes) {
            if (def.mode.key !in existingKeys) {
                val modeId = insertModeWithPreferences(def.mode, def.prefs)
                if (def.mode.key == "COMMAND" && preferencesRepository.activeModeId.first() == null) {
                    preferencesRepository.updateActiveModeId(modeId)
                }
            }
        }
    }
    private suspend fun insertModeWithPreferences(
        mode: ModeEntity,
        preference: ModePreferenceEntity,
    ): Long {
        val modeId = repository.insertMode(mode)
        repository.insertPreference(preference.copy(modeId = modeId))
        return modeId
    }

    /**
     * Seeds initial life logistics templates, providing boilerplate for common physical and
     * organizational tasks like travel packing.
     */
    private suspend fun seedLifeLogisticsTemplates() {
        val existingNames =
            repository
                .getAllTemplates()
                .first()
                .map { it.name.trim().lowercase() }
                .toSet()
        if ("travel pack template" !in existingNames) {
            repository.insertTemplate(
                TemplateEntity(
                    name = "Travel Pack Template",
                    nodeType = "note",
                    defaultTitle = "Travel pack - [Trip]",
                    defaultContent =
                        """
                        - IDs / documents
                        - Wallet / cards / cash
                        - Phone / charger / powerbank
                        - Medications
                        - Clothes / hygiene
                        - Special gear
                        - Don't forget items
                        """.trimIndent(),
                ),
            )
        }
    }
}
