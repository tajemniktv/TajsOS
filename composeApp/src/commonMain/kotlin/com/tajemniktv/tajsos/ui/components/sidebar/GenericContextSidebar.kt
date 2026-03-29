/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.components.sidebar

import androidx.compose.runtime.Composable
import com.tajemniktv.tajsos.ui.Screen

/**
 * Contextual sidebar renderer that provides dedicated section sets for main/root tabs.
 * Non-main routes fall back to placeholder sections.
 */
@Composable
internal fun GenericContextSidebar(
    screen: Screen,
    contextHeader: String,
    panelLabel: String,
    onBackToMainSidebar: () -> Unit,
) {
    ContextSidebarScaffold(
        contextHeader = contextHeader,
        panelLabel = panelLabel,
        onBackToMainSidebar = onBackToMainSidebar,
        sections = dedicatedSectionsFor(screen) ?: placeholderSectionsFor(screen),
    )
}

private fun dedicatedSectionsFor(screen: Screen): List<SidebarSection>? =
    when (screen) {
        Screen.Dashboard ->
            sidebarSections(
                primary = listOf("system overview", "quick launch", "daily priorities"),
                workflow = listOf("capture queue", "active lanes", "startup routines"),
                insights = listOf("health snapshot", "trend alerts", "recommended focus"),
            )
        Screen.Inbox ->
            sidebarSections(
                primary = listOf("capture inbox", "triage shortcuts", "newest entries"),
                workflow = listOf("processing states", "batch actions", "routing rules"),
                insights = listOf("inbox pressure", "stale entries", "clearance forecast"),
            )
        Screen.Search ->
            sidebarSections(
                primary = listOf("saved searches", "recent queries", "high-signal filters"),
                workflow = listOf("scope presets", "operator cheatsheet", "result pivots"),
                insights = listOf("query quality", "missed entities", "index coverage"),
            )
        Screen.Today ->
            sidebarSections(
                primary = listOf("today overview", "must-do tasks", "deadline radar"),
                workflow = listOf("time blocks", "sequence planner", "handoff staging"),
                insights = listOf("execution pace", "slip risks", "energy fit"),
            )
        Screen.Tasks ->
            sidebarSections(
                primary = listOf("task board", "quick filters", "due soon"),
                workflow = listOf("bulk status actions", "assignment lanes", "dependency chains"),
                insights = listOf("throughput", "blocked tasks", "completion pattern"),
            )
        Screen.Focus ->
            sidebarSections(
                primary = listOf("focus sprint", "deep-work slots", "distraction shield"),
                workflow = listOf("session setup", "interrupt guard", "focus rituals"),
                insights = listOf("session depth", "break quality", "focus drift"),
            )
        Screen.Decisions ->
            sidebarSections(
                primary = listOf("decision queue", "open options", "pending calls"),
                workflow = listOf("criteria checklists", "stakeholder notes", "decision templates"),
                insights = listOf("decision latency", "reversal rate", "confidence signals"),
            )
        Screen.OpenLoops ->
            sidebarSections(
                primary = listOf("open loops map", "critical loops", "aging loops"),
                workflow = listOf("closure actions", "owner routing", "follow-up cadence"),
                insights = listOf("loop pressure", "closure velocity", "risk loops"),
            )
        Screen.Calendar ->
            sidebarSections(
                primary = listOf("agenda snapshot", "upcoming blocks", "conflict flags"),
                workflow = listOf("reschedule tools", "buffer planning", "calendar sync"),
                insights = listOf("load balance", "meeting density", "focus-time erosion"),
            )
        Screen.Projects ->
            sidebarSections(
                primary = listOf("project overview", "active projects", "quick filters"),
                workflow = listOf("milestones", "blocked lanes", "coordination actions"),
                insights = listOf("velocity", "delivery risk", "project recommendations"),
            )
        Screen.Areas ->
            sidebarSections(
                primary = listOf("responsibility map", "active areas", "area filters"),
                workflow = listOf("maintenance cycles", "standards", "ownership actions"),
                insights = listOf("area stability", "neglect signals", "coverage gaps"),
            )
        Screen.Protocols ->
            sidebarSections(
                primary = listOf("protocol library", "active runs", "trigger shortcuts"),
                workflow = listOf("run controls", "step checkpoints", "failure recovery"),
                insights = listOf("protocol reliability", "completion ratios", "friction points"),
            )
        Screen.TimeArchitecture ->
            sidebarSections(
                primary = listOf("time model", "horizon views", "cadence controls"),
                workflow = listOf("planning layers", "window handoffs", "time constraints"),
                insights = listOf("time debt", "slack health", "planning drift"),
            )
        Screen.Places ->
            sidebarSections(
                primary = listOf("place index", "active locations", "context filters"),
                workflow = listOf("location routines", "visit plans", "context triggers"),
                insights = listOf("location fit", "travel overhead", "place utilization"),
            )
        Screen.Notes ->
            sidebarSections(
                primary = listOf("note workspace", "recent notes", "linked references"),
                workflow = listOf("capture to note", "organize passes", "summarization actions"),
                insights = listOf("knowledge growth", "orphan notes", "link density"),
            )
        Screen.Vaults ->
            sidebarSections(
                primary = listOf("vault index", "sensitive items", "vault filters"),
                workflow = listOf("access workflows", "review cadence", "retention rules"),
                insights = listOf("vault activity", "stale secrets", "security posture"),
            )
        Screen.Rules ->
            sidebarSections(
                primary = listOf("rulebook", "active rules", "rule shortcuts"),
                workflow = listOf("rule evaluation", "exception paths", "policy updates"),
                insights = listOf("rule adherence", "conflict hotspots", "effectiveness"),
            )
        Screen.Track ->
            sidebarSections(
                primary = listOf("tracking panel", "active metrics", "period filters"),
                workflow = listOf("log shortcuts", "metric upkeep", "tracking routines"),
                insights = listOf("trend shifts", "anomalies", "signal quality"),
            )
        Screen.Insights ->
            sidebarSections(
                primary = listOf("insight feed", "key findings", "priority signals"),
                workflow = listOf("analysis presets", "drilldown jumps", "report actions"),
                insights = listOf("confidence", "signal freshness", "prediction notes"),
            )
        Screen.Capacity ->
            sidebarSections(
                primary = listOf("capacity dashboard", "load map", "energy windows"),
                workflow = listOf("allocation actions", "workload balancing", "limits"),
                insights = listOf("overload risk", "recovery trends", "headroom"),
            )
        Screen.Identity ->
            sidebarSections(
                primary = listOf("identity overview", "values anchors", "role filters"),
                workflow = listOf("identity rituals", "alignment checks", "course correction"),
                insights = listOf("alignment trend", "identity drift", "coherence score"),
            )
        Screen.Graph ->
            sidebarSections(
                primary = listOf("relation graph", "entity spotlight", "link filters"),
                workflow = listOf("link actions", "cluster views", "graph maintenance"),
                insights = listOf("central nodes", "isolated clusters", "relation quality"),
            )
        Screen.Review ->
            sidebarSections(
                primary = listOf("review timeline", "weekly review", "open outcomes"),
                workflow = listOf("retrospective prompts", "closure actions", "plan handoff"),
                insights = listOf("outcome quality", "unfinished loops", "trend summary"),
            )
        Screen.Finances ->
            sidebarSections(
                primary = listOf("finance snapshot", "accounts focus", "liquidity view"),
                workflow = listOf("bill workflows", "budget actions", "renewal tracking"),
                insights = listOf("cash trend", "spend drift", "upcoming obligations"),
            )
        Screen.Health ->
            sidebarSections(
                primary = listOf("health dashboard", "care tasks", "wellbeing markers"),
                workflow = listOf("medication actions", "appointment flow", "habit checks"),
                insights = listOf("recovery signals", "symptom trends", "care recommendations"),
            )
        Screen.Relationships ->
            sidebarSections(
                primary = listOf("relationships map", "active threads", "priority people"),
                workflow = listOf("follow-up actions", "shared plans", "cadence settings"),
                insights = listOf("connection health", "drop-off signals", "next best touch"),
            )
        Screen.Education ->
            sidebarSections(
                primary = listOf("learning dashboard", "active courses", "study focus"),
                workflow = listOf("session plans", "practice loops", "material queues"),
                insights = listOf("learning pace", "retention signals", "skill gaps"),
            )
        Screen.Archive ->
            sidebarSections(
                primary = listOf("archive index", "recently archived", "restore filters"),
                workflow = listOf("cleanup actions", "retention windows", "restore flow"),
                insights = listOf("archive growth", "recovery rate", "cold-data value"),
            )
        Screen.Settings ->
            sidebarSections(
                primary = listOf("settings overview", "system controls", "quick toggles"),
                workflow = listOf("configuration actions", "preferences sync", "safety checks"),
                insights = listOf("config health", "drift warnings", "recommended settings"),
            )
        Screen.Profile ->
            sidebarSections(
                primary = listOf("profile summary", "identity details", "personal defaults"),
                workflow = listOf("profile edits", "preferences", "account actions"),
                insights = listOf("profile completeness", "behavior patterns", "personal signals"),
            )
        else -> null
    }

private fun sidebarSections(
    primary: List<String>,
    workflow: List<String>,
    insights: List<String>,
): List<SidebarSection> =
    listOf(
        SidebarSection(title = "PRIMARY", items = primary),
        SidebarSection(title = "WORKFLOW", items = workflow),
        SidebarSection(title = "INSIGHTS", items = insights),
    )

private fun placeholderSectionsFor(screen: Screen): List<SidebarSection> {
    val screenTag = screen.route.substringBefore("/")
    return listOf(
        SidebarSection(
            title = "PRIMARY",
            items =
                listOf(
                    "$screenTag overview placeholder",
                    "$screenTag shortcuts placeholder",
                    "$screenTag quick filters placeholder",
                ),
        ),
        SidebarSection(
            title = "WORKFLOW",
            items =
                listOf(
                    "$screenTag actions placeholder",
                    "$screenTag pinned context placeholder",
                    "$screenTag automation placeholder",
                ),
        ),
        SidebarSection(
            title = "INSIGHTS",
            items =
                listOf(
                    "$screenTag metrics placeholder",
                    "$screenTag anomalies placeholder",
                    "$screenTag recommendations placeholder",
                ),
        ),
    )
}
