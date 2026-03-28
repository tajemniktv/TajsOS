/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui

/**
 * A default list of suggested Area titles provided during the initial onboarding or area creation flow.
 */
val suggestedAreaTitles =
    listOf(
        "Career",
        "Hobbies",
    )

/**
 * A predefined list of standard [TransitionProtocolTemplate] objects used to bootstrap new transition protocols.
 */
val defaultTransitionProtocolTemplates =
    listOf(
        TransitionProtocolTemplate(
            key = "morning_startup",
            label = "Morning startup",
            checklist =
                listOf(
                    "Drink water and take meds if needed",
                    "Review today's top 3 priorities",
                    "Clear inbox noise for 5 minutes",
                    "Start first focused task",
                ),
        ),
        TransitionProtocolTemplate(
            key = "before_class",
            label = "Before class",
            checklist =
                listOf(
                    "Open class materials and notes",
                    "Prepare one question to clarify",
                    "Set phone to focus mode",
                    "Confirm arrival buffer",
                ),
        ),
        TransitionProtocolTemplate(
            key = "before_leaving_home",
            label = "Before leaving home",
            checklist =
                listOf(
                    "Check keys, wallet, phone",
                    "Bring required documents/devices",
                    "Confirm destination and next stop",
                    "Capture any open loop before leaving",
                ),
        ),
        TransitionProtocolTemplate(
            key = "deep_work_entry",
            label = "Deep work entry",
            checklist =
                listOf(
                    "Choose one clear deliverable",
                    "Close distracting tabs and notifications",
                    "Set focus timer",
                    "Write the first concrete step",
                ),
        ),
        TransitionProtocolTemplate(
            key = "shutdown_ritual",
            label = "Shutdown ritual",
            checklist =
                listOf(
                    "Capture unfinished thoughts and loops",
                    "Reschedule or pin tomorrow's top tasks",
                    "Close active sessions",
                    "Set mode for next morning",
                ),
        ),
        TransitionProtocolTemplate(
            key = "recovery_after_derailment",
            label = "Recovery after derailment",
            checklist =
                listOf(
                    "Pause and run a 2-minute reset",
                    "Pick one tiny stabilizing task",
                    "Mark one open loop as next action",
                    "Restart with low-friction work",
                ),
        ),
        TransitionProtocolTemplate(
            key = "exam_week",
            label = "Exam week",
            checklist =
                listOf(
                    "Review exam schedule and deadlines",
                    "Select top revision targets",
                    "Timebox admin and social noise",
                    "Plan recovery blocks",
                ),
        ),
        TransitionProtocolTemplate(
            key = "travel_day",
            label = "Travel day",
            checklist =
                listOf(
                    "Confirm tickets, IDs, and timing",
                    "Prepare packing and chargers",
                    "Queue commute-friendly tasks",
                    "Set lightweight mode for transit",
                ),
        ),
        TransitionProtocolTemplate(
            key = "before_sleep",
            label = "Before sleep",
            checklist =
                listOf(
                    "Dump remaining mental load",
                    "Pin one first task for tomorrow",
                    "Set alarms and essentials",
                    "Close with low-stimulation routine",
                ),
        ),
        TransitionProtocolTemplate(
            key = "after_interruption",
            label = "After interruption",
            checklist =
                listOf(
                    "Re-open last task context",
                    "Restate next smallest step",
                    "Set 10-minute re-entry timer",
                    "Resume before checking messages",
                ),
        ),
        TransitionProtocolTemplate(
            key = "arriving_on_campus",
            label = "Arriving on campus",
            checklist =
                listOf(
                    "Check class location and timing",
                    "Open assignment/revision queue",
                    "Capture urgent follow-ups",
                    "Switch to study context",
                ),
        ),
        TransitionProtocolTemplate(
            key = "scrolling_to_working",
            label = "Switching from scrolling to working",
            checklist =
                listOf(
                    "Close social apps",
                    "Define one work target",
                    "Start 10-minute focus sprint",
                    "Block distractions",
                ),
        ),
        TransitionProtocolTemplate(
            key = "work_to_rest",
            label = "Switching from work to rest",
            checklist =
                listOf(
                    "Capture loose ends",
                    "Mark today's progress",
                    "Set first task for next session",
                    "Transition to rest intentionally",
                ),
        ),
    )

/**
 * A predefined list of standard [PlaybookTemplate] objects providing actionable step-by-step guides for common scenarios.
 */
val defaultPlaybookTemplates =
    listOf(
        PlaybookTemplate(
            key = "bad_day",
            label = "Bad day protocol",
            checklist =
                listOf(
                    "Drop scope to essentials",
                    "Hydrate + meds check",
                    "Choose one tiny win",
                    "Close one open loop",
                ),
            recommendedModeKey = "RECOVERY",
        ),
        PlaybookTemplate(
            key = "panic_ish_day",
            label = "Panic-ish day protocol",
            checklist =
                listOf(
                    "Box breathing 2 minutes",
                    "List immediate threats",
                    "Pick one stabilizing action",
                    "Silence non-critical channels",
                ),
            recommendedModeKey = "RECOVERY",
        ),
        PlaybookTemplate(
            key = "cant_start_studying",
            label = "Can't start studying protocol",
            checklist =
                listOf(
                    "Open material only",
                    "Set 10-minute timer",
                    "Write first question",
                    "Start with easiest section",
                ),
            recommendedModeKey = "STUDY",
        ),
        PlaybookTemplate(
            key = "need_to_leave_house",
            label = "Need to leave house protocol",
            checklist =
                listOf(
                    "Essentials check",
                    "Destination and route",
                    "Bring list verification",
                    "Queue out-of-home tasks",
                ),
            recommendedModeKey = "ERRAND",
        ),
        PlaybookTemplate(
            key = "weekly_reset",
            label = "Weekly reset protocol",
            checklist =
                listOf(
                    "Review completed work",
                    "Rebuild this-week map",
                    "Clear inbox noise",
                    "Set top priorities",
                ),
            recommendedModeKey = "COMMAND",
        ),
        PlaybookTemplate(
            key = "exam_prep",
            label = "Exam prep protocol",
            checklist =
                listOf(
                    "Identify exam targets",
                    "Plan revision sessions",
                    "Prepare question list",
                    "Protect recovery windows",
                ),
            recommendedModeKey = "EXAM_WEEK",
        ),
        PlaybookTemplate(
            key = "project_kickoff",
            label = "Project kickoff protocol",
            checklist =
                listOf(
                    "Define outcome",
                    "Break first milestone",
                    "Create first actions",
                    "Schedule review checkpoint",
                ),
            recommendedModeKey = "DEEP_WORK",
        ),
        PlaybookTemplate(
            key = "life_collapsing",
            label = "Life is collapsing protocol",
            checklist =
                listOf(
                    "Stabilize basics first",
                    "Pause new intake",
                    "Escalate only critical items",
                    "Ask for support where possible",
                ),
            recommendedModeKey = "RECOVERY",
        ),
        PlaybookTemplate(
            key = "low_energy_must_function",
            label = "Low energy but must function protocol",
            checklist =
                listOf(
                    "Switch to low-friction tasks",
                    "Use short sprints",
                    "Batch context",
                    "Minimize switching",
                ),
            recommendedModeKey = "LOW_BATTERY",
        ),
        PlaybookTemplate(
            key = "need_to_reply_everyone",
            label = "Need to reply to everyone protocol",
            checklist =
                listOf(
                    "Open reply queue",
                    "Sort by urgency",
                    "Send short acknowledgements first",
                    "Convert leftovers to follow-ups",
                ),
            recommendedModeKey = "SOCIAL",
        ),
        PlaybookTemplate(
            key = "back_on_track_after_derailment",
            label = "Get back on track after derailment protocol",
            checklist =
                listOf(
                    "Capture derailment fallout",
                    "Pick restart point",
                    "Run 15-minute reset",
                    "Resume with one concrete action",
                ),
            recommendedModeKey = "RECOVERY",
        ),
    )
