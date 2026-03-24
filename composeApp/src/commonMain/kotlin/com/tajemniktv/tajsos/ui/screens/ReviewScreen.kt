/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tajemniktv.tajsos.ui.MainViewModel
import com.tajemniktv.tajsos.ui.theme.TactileTheme
import org.jetbrains.compose.resources.stringResource
import tajsos.composeapp.generated.resources.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit,
) {
    var reviewType by remember { mutableStateOf<String?>(null) }
    var currentStep by remember { mutableStateOf(0) }

    val dashboardState by viewModel.dashboardUIState.collectAsState()
    val insights by viewModel.insights.collectAsState()

    val dailySteps = listOf(
        Res.string.review_step_mood_energy,
        Res.string.review_step_wins,
        Res.string.review_step_blockers,
        Res.string.review_step_plan
    )
    val weeklySteps = listOf(
        Res.string.review_step_stats,
        Res.string.review_step_cleanup,
        Res.string.review_step_archive,
        Res.string.review_step_goals
    )

    val reviewTitle = when (reviewType) {
        "daily" -> stringResource(Res.string.review_daily)
        "weekly" -> stringResource(Res.string.review_weekly)
        "monthly" -> stringResource(Res.string.review_monthly)
        else -> stringResource(Res.string.review_select_type)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(reviewTitle.uppercase()) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(Res.string.common_back)
                        )
                    }
                }
            )
        }
    ) { padding ->
        if (reviewType == null) {
            Column(
                modifier = Modifier.fillMaxSize().padding(padding).padding(TactileTheme.SpacingMd),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ReviewTypeButton(
                    stringResource(Res.string.review_daily),
                    Icons.Default.WbSunny
                ) { reviewType = "daily" }
                Spacer(Modifier.height(16.dp))
                ReviewTypeButton(
                    stringResource(Res.string.review_weekly),
                    Icons.Default.DateRange
                ) { reviewType = "weekly" }
                Spacer(Modifier.height(16.dp))
                ReviewTypeButton(
                    stringResource(Res.string.review_monthly),
                    Icons.Default.CalendarMonth
                ) { reviewType = "monthly" }
            }
        } else {
            val steps = if (reviewType == "daily") dailySteps else weeklySteps
            ReviewFlow(
                type = reviewType!!,
                steps = steps,
                currentStep = currentStep,
                viewModel = viewModel,
                dashboardState = dashboardState,
                insights = insights,
                onNext = { if (currentStep < steps.size - 1) currentStep++ },
                onComplete = { content, mood, energy ->
                    viewModel.completeReview(reviewType!!, content, mood, energy)
                    onBack()
                }
            )
        }
    }
}

@Composable
fun ReviewTypeButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(80.dp),
        shape = RoundedCornerShape(TactileTheme.RadiusMd)
    ) {
        Icon(icon, contentDescription = null)
        Spacer(Modifier.width(16.dp))
        Text(label, style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
fun ReviewFlow(
    type: String,
    steps: List<org.jetbrains.compose.resources.StringResource>,
    currentStep: Int,
    viewModel: MainViewModel,
    dashboardState: com.tajemniktv.tajsos.ui.DashboardUIState,
    insights: com.tajemniktv.tajsos.ui.InsightsData,
    onNext: () -> Unit,
    onComplete: (String, Int?, Int?) -> Unit
) {
    var mood by remember { mutableStateOf(3) }
    var energy by remember { mutableStateOf(3) }
    var answers = remember { mutableStateMapOf<Int, String>() }

    Column(modifier = Modifier.fillMaxSize().padding(TactileTheme.SpacingMd)) {
        LinearProgressIndicator(
            progress = { (currentStep + 1).toFloat() / steps.size },
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            color = TactileTheme.Primary
        )

        Text(
            stringResource(steps[currentStep]),
            style = MaterialTheme.typography.headlineSmall,
            color = TactileTheme.Primary
        )

        Spacer(Modifier.height(16.dp))

        Box(Modifier.weight(1f)) {
            when (steps[currentStep]) {
                Res.string.review_step_mood_energy -> {
                    Column {
                        Text("How are you feeling right now?")
                        Slider(
                            value = mood.toFloat(),
                            onValueChange = { mood = it.toInt() },
                            valueRange = 1f..5f,
                            steps = 3
                        )
                        Text("Mood: $mood/5")
                        Spacer(Modifier.height(24.dp))
                        Text("What is your energy level?")
                        Slider(
                            value = energy.toFloat(),
                            onValueChange = { energy = it.toInt() },
                            valueRange = 1f..5f,
                            steps = 3
                        )
                        Text("Energy: $energy/5")
                    }
                }

                Res.string.review_step_stats -> {
                    Column(Modifier.verticalScroll(rememberScrollState())) {
                        Text("Capacity: ${insights.weeklyCompletions} tasks done.")
                        Text("Execution Ratio: ${(insights.captureToActionRatio * 100).toInt()}%")
                        Text("Focus: ${insights.weeklyFocusHours.toInt()} hours.")
                        Spacer(Modifier.height(16.dp))
                        Text("How do you feel about these numbers?")
                        TextField(
                            value = answers[currentStep] ?: "",
                            onValueChange = { answers[currentStep] = it },
                            modifier = Modifier.fillMaxWidth().height(150.dp)
                        )
                    }
                }

                Res.string.review_step_archive -> {
                    val suggestions =
                        dashboardState.archivedThisWeek // Just showing what was done, but could suggest others
                    Column {
                        Text("You archived ${suggestions.size} items this week. Anything else to let go of?")
                        LazyColumn(Modifier.height(200.dp)) {
                            items(dashboardState.neglectedThisWeek.take(5)) { item ->
                                ListItem(
                                    headlineContent = { Text(item.node.title) },
                                    trailingContent = {
                                        IconButton(onClick = { viewModel.archiveNode(item.node) }) {
                                            Icon(
                                                Icons.Default.Delete,
                                                contentDescription = "Archive",
                                                tint = TactileTheme.Error
                                            )
                                        }
                                    }
                                )
                            }
                        }
                        TextField(
                            value = answers[currentStep] ?: "",
                            onValueChange = { answers[currentStep] = it },
                            modifier = Modifier.fillMaxWidth().height(100.dp)
                        )
                    }
                }

                Res.string.review_step_wins, Res.string.review_step_blockers, Res.string.review_step_plan, Res.string.review_step_cleanup, Res.string.review_step_goals -> {
                    TextField(
                        value = answers[currentStep] ?: "",
                        onValueChange = { answers[currentStep] = it },
                        modifier = Modifier.fillMaxWidth().fillMaxHeight(),
                        placeholder = { Text("Write your thoughts here...") }
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
            horizontalArrangement = Arrangement.End
        ) {
            if (currentStep < steps.size - 1) {
                Button(onClick = onNext) {
                    Text(stringResource(Res.string.review_next))
                }
            } else {
                val completeLabel = stringResource(Res.string.review_complete)
                Button(onClick = {
                    // Note: joinToString here will use the resource objects, not their string values.
                    // This is a bit tricky since stringResource can only be called in @Composable.
                    // For now, let's keep it simple or assume the user wants the content to be in English for the DB.
                    val content = steps.indices.joinToString("\n\n") { i ->
                        "Step $i:\n${answers[i] ?: "N/A"}"
                    }
                    onComplete(content, mood, energy)
                }) {
                    Text(completeLabel)
                }
            }
        }
    }
}
