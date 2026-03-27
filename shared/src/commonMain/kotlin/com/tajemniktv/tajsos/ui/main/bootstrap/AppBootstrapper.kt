/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui

import com.tajemniktv.tajsos.data.AppRepository
import com.tajemniktv.tajsos.data.ModeEntity
import com.tajemniktv.tajsos.data.ModePreferenceEntity
import com.tajemniktv.tajsos.data.NodeEntity
import com.tajemniktv.tajsos.data.NodeWithPin
import com.tajemniktv.tajsos.data.PreferencesRepository
import com.tajemniktv.tajsos.data.RelationEntity
import com.tajemniktv.tajsos.data.TemplateEntity
import com.tajemniktv.tajsos.data.UserEntity
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull

/**
 * A utility class responsible for initializing and populating the local database with core operational data
 * during the initial onboarding flow or upon system launch.
 *
 * It generates default entities such as focus modes, academic templates, logistical systems,
 * and base user data if the local storage is detected as entirely empty.
 *
 * @property repository The [AppRepository] used for direct database inserts.
 * @property preferencesRepository The [PreferencesRepository] used to handle default tier access checks.
 * @property allNodes A reactive [StateFlow] representing the current list of all nodes, used to determine if the DB is empty.
 * @property user A reactive [StateFlow] representing the current user entity state.
 */
class AppBootstrapper(
    private val repository: AppRepository,
    private val preferencesRepository: PreferencesRepository,
    private val allNodes: StateFlow<List<NodeWithPin>>,
    private val user: StateFlow<UserEntity?>,
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
        if (allNodes.value.isEmpty()) {
            seedOnboardingData()
        }
    }

    /**
     * Creates the baseline [UserEntity] if it doesn't already exist.
     */
    private suspend fun seedUserData() {
        if (user.first() == null) {
            repository.insertUser(UserEntity(name = "OPERATOR"))
        }
    }

    private suspend fun seedOnboardingData() {
        if (allNodes.value.isNotEmpty()) return

        seedDefaultModes()

        val welcomeId =
            repository.insertNode(
                NodeEntity(
                    title = "Welcome to TajsOS",
                    content = "This is your new Second Brain. Capture everything, organize later.",
                    type = "note",
                    inboxState = false,
                    isPinned = true,
                ),
            )

        val taskId =
            repository.insertNode(
                NodeEntity(
                    title = "Explore the Dashboard",
                    type = "task",
                    inboxState = true,
                ),
            )

        val areaId =
            repository.insertNode(
                NodeEntity(
                    title = "Personal",
                    type = "area",
                    inboxState = false,
                ),
            )

        repository.insertNode(
            NodeEntity(
                title = "Reply to research email",
                type = "open_loop",
                openLoopType = "reply_needed",
                areaId = areaId,
                inboxState = false,
            ),
        )

        repository.insertNode(
            NodeEntity(
                title = "Choose between Hilt and Koin",
                type = "decision",
                decisionStatus = "pending",
                areaId = areaId,
                inboxState = false,
            ),
        )

        repository.insertNode(
            NodeEntity(
                title = "Monthly server backup",
                type = "maintenance",
                maintenanceType = "backup",
                areaId = areaId,
                inboxState = false,
            ),
        )

        repository.insertRelation(
            RelationEntity(
                fromNodeId = welcomeId,
                toNodeId = taskId,
                relationType = "RELATED",
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

        templates.forEach { template ->
            if (!existingNames.contains(template.name.trim().lowercase())) {
                repository.insertTemplate(template)
            }
        }
    }

    /**
     * Identifies if default system modes exist (e.g., Work, Recovery, Chaos), and inserts them
     * alongside their associated [ModePreferenceEntity] configurations if they are missing.
     */
    private suspend fun seedDefaultModes() {
        val existingModes = repository.getAllModes().first()
        val existingKeys = existingModes.map { it.key }

        if ("COMMAND" !in existingKeys) {
            val commandId =
                repository.insertMode(
                    ModeEntity(
                        key = "COMMAND",
                        name = "Command",
                        description = "Default everyday overview mode. What matters right now?",
                        icon = "dashboard",
                        sortOrder = 0,
                        themeColor = 0xFF3F51B5.toInt(),
                    ),
                )
            repository.insertPreference(
                ModePreferenceEntity(
                    modeId = commandId,
                    showInbox = true,
                    showStats = true,
                    dashboardBlocksJson = "[\"today_top_3\", \"resume_context\", \"inbox_count\", \"deadlines\", \"overdue\", \"pinned_note\"]",
                ),
            )
            if (preferencesRepository.activeModeId.first() == null) {
                preferencesRepository.updateActiveModeId(commandId)
            }
        }

        if ("FOCUS" !in existingKeys) {
            val focusId =
                repository.insertMode(
                    ModeEntity(
                        key = "FOCUS",
                        name = "Focus",
                        description = "Narrow the system to one thing. keep attention on this.",
                        icon = "center_focus_strong",
                        sortOrder = 1,
                        themeColor = 0xFFF44336.toInt(),
                    ),
                )
            repository.insertPreference(
                ModePreferenceEntity(
                    modeId = focusId,
                    showInbox = false,
                    showStats = false,
                    dashboardBlocksJson = "[\"current_task\", \"next_step\", \"timer\", \"blockers\", \"linked_resources\"]",
                ),
            )
        }

        if ("RECOVERY" !in existingKeys) {
            val recoveryId =
                repository.insertMode(
                    ModeEntity(
                        key = "RECOVERY",
                        name = "Recovery",
                        description = "Support low-capacity functioning. Smallest safe useful thing.",
                        icon = "medical_services",
                        sortOrder = 2,
                        themeColor = 0xFF4CAF50.toInt(),
                    ),
                )
            repository.insertPreference(
                ModePreferenceEntity(
                    modeId = recoveryId,
                    showInbox = false,
                    showStats = false,
                    dashboardBlocksJson = "[\"basics\", \"easy_wins\", \"urgent_only\", \"recovery_protocol\", \"check_in\"]",
                ),
            )
        }

        if ("STUDY" !in existingKeys) {
            val studyId =
                repository.insertMode(
                    ModeEntity(
                        key = "STUDY",
                        name = "Study",
                        description = "Focus on learning and academic performance.",
                        icon = "school",
                        sortOrder = 3,
                        themeColor = 0xFFFF9800.toInt(),
                    ),
                )
            repository.insertPreference(
                ModePreferenceEntity(
                    modeId = studyId,
                    dashboardBlocksJson = "[\"classes\", \"assignments\", \"deadlines\", \"notes\", \"revision_targets\"]",
                ),
            )
        }

        if ("ERRAND" !in existingKeys) {
            val errandId =
                repository.insertMode(
                    ModeEntity(
                        key = "ERRAND",
                        name = "Errand",
                        description = "Out-of-home execution and logistical clustering.",
                        icon = "shopping_cart",
                        sortOrder = 4,
                        themeColor = 0xFF00BCD4.toInt(),
                    ),
                )
            repository.insertPreference(
                ModePreferenceEntity(
                    modeId = errandId,
                    dashboardBlocksJson = "[\"shopping_list\", \"place_based_tasks\", \"errands\", \"what_to_bring\"]",
                ),
            )
        }

        if ("ADMIN" !in existingKeys) {
            val adminId =
                repository.insertMode(
                    ModeEntity(
                        key = "ADMIN",
                        name = "Admin",
                        description = "Handle the 'paperwork' of life. Subscriptions, bills, forms.",
                        icon = "gavel",
                        sortOrder = 5,
                        themeColor = 0xFF607D8B.toInt(),
                    ),
                )
            repository.insertPreference(
                ModePreferenceEntity(
                    modeId = adminId,
                    dashboardBlocksJson = "[\"paperwork\", \"bills\", \"renewals\", \"subscriptions\", \"bureaucracy\"]",
                ),
            )
        }

        if ("SHUTDOWN" !in existingKeys) {
            val shutdownId =
                repository.insertMode(
                    ModeEntity(
                        key = "SHUTDOWN",
                        name = "Shutdown",
                        description = "Nightly reset and preparation for tomorrow.",
                        icon = "bedtime",
                        sortOrder = 6,
                        themeColor = 0xFF673AB7.toInt(),
                    ),
                )
            repository.insertPreference(
                ModePreferenceEntity(
                    modeId = shutdownId,
                    dashboardBlocksJson = "[\"tomorrow_prep\", \"mini_review\", \"dump_leftovers\", \"open_loops_reduction\"]",
                ),
            )
        }

        if ("LOW_BATTERY" !in existingKeys) {
            val lowBatteryId =
                repository.insertMode(
                    ModeEntity(
                        key = "LOW_BATTERY",
                        name = "Low Battery",
                        description = "Minimal survival mode for when you are emotionally or physically drained.",
                        icon = "battery_alert",
                        sortOrder = 7,
                        themeColor = 0xFFE91E63.toInt(),
                    ),
                )
            repository.insertPreference(
                ModePreferenceEntity(
                    modeId = lowBatteryId,
                    showInbox = false,
                    dashboardBlocksJson = "[\"survival_basics\", \"tiny_wins\", \"passive_input\", \"comfort_notes\"]",
                ),
            )
        }

        if ("ALL" !in existingKeys) {
            val allModeId =
                repository.insertMode(
                    ModeEntity(
                        key = "ALL",
                        name = "All",
                        description = "Unfiltered access to the entire system. No restrictions.",
                        icon = "all_inclusive",
                        sortOrder = 8,
                        themeColor = 0xFF9E9E9E.toInt(),
                    ),
                )
            repository.insertPreference(
                ModePreferenceEntity(
                    modeId = allModeId,
                    showInbox = true,
                    showStats = true,
                    dashboardBlocksJson = "[\"today_top_3\", \"search\", \"alerts\", \"focus\", \"insights\", \"knowledge\", \"operational\"]",
                ),
            )
        }
    }

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