package com.tajemniktv.tajsos.ui.main.calculators

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import kotlin.time.Duration.Companion.days

import kotlin.time.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import com.tajemniktv.tajsos.data.ModeEntity
import com.tajemniktv.tajsos.data.TrackEntryEntity

class MainProtocolHelpersTest {

    @Test
    fun testNormalizeProtocolLabel() {
        assertEquals("hello world", normalizeProtocolLabel("Hello World!"))
        assertEquals("hello world", normalizeProtocolLabel("  Hello... World  "))
        assertEquals("hello 123", normalizeProtocolLabel("Hello 123"))
        assertEquals("", normalizeProtocolLabel("   !!!   "))
    }

    @Test
    fun testBuildPlaybookRelationshipContext() {
        assertEquals("playbook", buildPlaybookRelationshipContext(null))
        assertEquals("playbook", buildPlaybookRelationshipContext(""))
        assertEquals("playbook", buildPlaybookRelationshipContext("   "))
        assertEquals("playbook|mode=WORK", buildPlaybookRelationshipContext("work"))
        assertEquals("playbook|mode=WORK", buildPlaybookRelationshipContext(" WORK "))
    }

    @Test
    fun testParsePlaybookModeKey() {
        assertNull(parsePlaybookModeKey(null))
        assertNull(parsePlaybookModeKey(""))
        assertNull(parsePlaybookModeKey("playbook"))
        assertNull(parsePlaybookModeKey("playbook|mode="))
        assertNull(parsePlaybookModeKey("playbook|mode=   "))

        assertEquals("WORK", parsePlaybookModeKey("playbook|mode=work"))
        assertEquals("WORK", parsePlaybookModeKey("playbook|mode=WORK"))
        assertEquals("WORK", parsePlaybookModeKey("playbook|mode= WORK "))
    }

    @Test
    fun testProtocolChecklistProgress() {
        val emptyContent = ""
        val (emptyDone, emptyTotal) = protocolChecklistProgress(emptyContent)
        assertEquals(0, emptyDone)
        assertEquals(0, emptyTotal)

        val oneDone = "- [x] task"
        val (oneD, oneT) = protocolChecklistProgress(oneDone)
        assertEquals(1, oneD)
        assertEquals(1, oneT)

        val multiple = """
            ## Checklist
            - [ ] Task 1
            - [x] Task 2
            - [ ] Task 3
            Some text
        """.trimIndent()
        val (multiDone, multiTotal) = protocolChecklistProgress(multiple)
        assertEquals(1, multiDone)
        assertEquals(3, multiTotal)
    }

    @Test
    fun testCalculateNextRecurringDate() {
        val epochMs = 1704067200000L // 2024-01-01 00:00:00 UTC

        // 1 day = 86400000 ms
        assertEquals(epochMs + 86400000L, calculateNextRecurringDate(epochMs, "daily"))
        assertEquals(epochMs + 86400000L, calculateNextRecurringDate(epochMs, "DAILY"))

        // 7 days = 604800000 ms
        assertEquals(epochMs + 604800000L, calculateNextRecurringDate(epochMs, "weekly"))

        // 30 days = 2592000000 ms
        assertEquals(epochMs + 2592000000L, calculateNextRecurringDate(epochMs, "monthly"))

        // fallback
        assertEquals(epochMs + 86400000L, calculateNextRecurringDate(epochMs, "unknown"))
    }

    @Test
    fun testFindProtocolTemplate() {
        val t1 = com.tajemniktv.tajsos.ui.main.state.TransitionProtocolTemplate("morning_startup", "Morning Startup", emptyList())
        val t2 = com.tajemniktv.tajsos.ui.main.state.TransitionProtocolTemplate("before_sleep", "Before Sleep", emptyList())
        val templates = listOf(t1, t2)

        // Find by exact label match
        assertEquals(t1, findProtocolTemplate(templates, "Morning Startup"))

        // Find by label with different case/spaces
        assertEquals(t1, findProtocolTemplate(templates, "  morning startup  "))

        // Find by key
        assertEquals(t2, findProtocolTemplate(templates, "before_sleep"))

        // Not found
        assertNull(findProtocolTemplate(templates, "unknown"))
    }

    @Test
    fun testFindPlaybookTemplate() {
        val t1 = com.tajemniktv.tajsos.ui.main.state.PlaybookTemplate("exam_prep", "Exam Prep", emptyList())
        val t2 = com.tajemniktv.tajsos.ui.main.state.PlaybookTemplate("project_kickoff", "Project Kickoff", emptyList())
        val templates = listOf(t1, t2)

        // Find by exact label match
        assertEquals(t1, findPlaybookTemplate(templates, "Exam Prep"))

        // Find by label with different case/spaces
        assertEquals(t1, findPlaybookTemplate(templates, "  exam prep  "))

        // Find by key
        assertEquals(t2, findPlaybookTemplate(templates, "project_kickoff"))

        // Not found
        assertNull(findPlaybookTemplate(templates, "unknown"))
    }

    @Test
    fun testBuildProtocolChecklistContent() {
        val template = com.tajemniktv.tajsos.ui.main.state.TransitionProtocolTemplate(
            key = "test",
            label = "Test Protocol",
            checklist = listOf("Step 1", "Step 2", "Step 3")
        )

        val content = buildProtocolChecklistContent(template)
        val expected = """
            ## TRANSITION CHECKLIST
            - [ ] Step 1
            - [ ] Step 2
            - [ ] Step 3
        """.trimIndent().trimEnd()

        assertEquals(expected, content)
    }

    @Test
    fun testSuggestPlaybookLabel() {
        val today =
            Clock.System
                .now()
                .toLocalDateTime(TimeZone.currentSystemDefault())
                .date
                .toString()

        // High anxiety
        val highAnxiety = TrackEntryEntity(id = 1, date = today, anxietyScore = 4, energyScore = 5, moodScore = 5, createdAt = 1000L)
        assertEquals("Panic-ish day protocol", suggestPlaybookLabel(null, listOf(highAnxiety)))

        // Low energy
        val lowEnergy = TrackEntryEntity(id = 2, date = today, anxietyScore = 1, energyScore = 2, moodScore = 5, createdAt = 2000L)
        assertEquals("Low energy but must function protocol", suggestPlaybookLabel(null, listOf(lowEnergy)))

        // Mode specific fallbacks
        val studyMode = ModeEntity(id = 1, key = "STUDY", name = "Study")
        assertEquals("Can't start studying protocol", suggestPlaybookLabel(studyMode, emptyList()))

        val errandMode = ModeEntity(id = 2, key = "ERRAND", name = "Errand")
        assertEquals("Need to leave house protocol", suggestPlaybookLabel(errandMode, emptyList()))

        val socialMode = ModeEntity(id = 3, key = "SOCIAL", name = "Social")
        assertEquals("Need to reply to everyone protocol", suggestPlaybookLabel(socialMode, emptyList()))

        // No match
        assertNull(suggestPlaybookLabel(null, emptyList()))
    }

    @Test
    fun testRecommendProtocolLabel() {
        val t1 = com.tajemniktv.tajsos.ui.main.state.TransitionProtocolTemplate("morning_startup", "Morning Startup", emptyList())
        val t2 = com.tajemniktv.tajsos.ui.main.state.TransitionProtocolTemplate("before_class", "Before Class", emptyList())
        val t3 = com.tajemniktv.tajsos.ui.main.state.TransitionProtocolTemplate("deep_work_entry", "Deep Work", emptyList())
        val t4 = com.tajemniktv.tajsos.ui.main.state.TransitionProtocolTemplate("work_to_rest", "Work to Rest", emptyList())
        val t5 = com.tajemniktv.tajsos.ui.main.state.TransitionProtocolTemplate("before_sleep", "Before Sleep", emptyList())
        val templates = listOf(t1, t2, t3, t4, t5)

        val recommended = recommendProtocolLabel(templates)

        val localNow = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val expectedLabel = when (localNow.hour) {
            in 5..9 -> "Morning Startup"
            in 10..14 -> "Before Class"
            in 15..18 -> "Deep Work"
            in 19..21 -> "Work to Rest"
            else -> "Before Sleep"
        }

        assertEquals(expectedLabel, recommended)

        // Not found case
        assertNull(recommendProtocolLabel(emptyList()))
    }

    @Test
    fun testSuggestPlaybookLabel_edgeCases() {
        val now = Clock.System.now()
        val tz = TimeZone.currentSystemDefault()
        val today = now.toLocalDateTime(tz).date.toString()
        val yesterday = (now - 1.days).toLocalDateTime(tz).date.toString()

        // Empty entries
        assertNull(suggestPlaybookLabel(null, emptyList()))

        // Old entries are ignored
        val oldAnxiety = TrackEntryEntity(id = 1, date = yesterday, anxietyScore = 5, energyScore = 5, moodScore = 5, createdAt = 1000L)
        assertNull(suggestPlaybookLabel(null, listOf(oldAnxiety)))

        // Multiple entries today, should pick latest by createdAt
        val earlyCalm = TrackEntryEntity(id = 2, date = today, anxietyScore = 1, energyScore = 5, moodScore = 5, createdAt = 1000L)
        val laterAnxiety = TrackEntryEntity(id = 3, date = today, anxietyScore = 4, energyScore = 5, moodScore = 5, createdAt = 2000L)
        val latestCalm = TrackEntryEntity(id = 4, date = today, anxietyScore = 1, energyScore = 5, moodScore = 5, createdAt = 3000L)

        // Latest is calm, so no panic suggested
        assertNull(suggestPlaybookLabel(null, listOf(earlyCalm, laterAnxiety, latestCalm)))

        // Latest is anxiety, panic suggested
        assertEquals("Panic-ish day protocol", suggestPlaybookLabel(null, listOf(earlyCalm, latestCalm, laterAnxiety.copy(createdAt = 4000L))))

        // Recovery / Bad Day modes
        val recoveryMode = ModeEntity(id = 1, key = "RECOVERY", name = "Recovery")
        assertEquals("Bad day protocol", suggestPlaybookLabel(recoveryMode, emptyList()))

        val lowBatteryMode = ModeEntity(id = 2, key = "LOW_BATTERY", name = "Low Battery")
        assertEquals("Bad day protocol", suggestPlaybookLabel(lowBatteryMode, emptyList()))

        val cantThinkMode = ModeEntity(id = 3, key = "CANT_THINK", name = "Cant Think")
        assertEquals("Bad day protocol", suggestPlaybookLabel(cantThinkMode, emptyList()))
    }


    @Test
    fun testMatchesQuery_delegatesToFilterHelper() {
        val node = com.tajemniktv.tajsos.ui.buildTestNode(id = 1, title = "Find This", content = "Hidden")

        assertTrue(matchesQuery(node, "Find This"))
        assertTrue(matchesQuery(node, "Hidden"))
        assertFalse(matchesQuery(node, "Missing"))
        assertFalse(matchesQuery(node, ""))
    }
}
