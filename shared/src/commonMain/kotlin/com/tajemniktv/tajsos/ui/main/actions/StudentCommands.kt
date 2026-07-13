/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.main.actions

import com.tajemniktv.tajsos.data.AppRepository
import com.tajemniktv.tajsos.data.NodeEntity
import com.tajemniktv.tajsos.data.NodeMetadataEnvelope
import com.tajemniktv.tajsos.data.StudentMetadata
import com.tajemniktv.tajsos.data.TagEntity
import com.tajemniktv.tajsos.data.metadataEnvelopeOrNull
import com.tajemniktv.tajsos.data.withMetadataEnvelope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.time.Clock

/**
 * A specialized command dispatcher responsible for all academic, study-tracking, and student-focused operations.
 *
 * It manages interactions with nodes holding `StudentMetadata` embedded in their JSON payload,
 * handles study progress updates, and structures academic connections (like papers to concepts).
 *
 * @property repository The [AppRepository] used for direct database updates.
 * @property scope The [CoroutineScope] in which all asynchronous database operations execute.
 * @property currentTags A lambda supplier providing real-time access to the list of all tags.
 * @property addNodeForResult A complex lambda function that creates a new node and immediately returns its inserted database ID.
 * @property startFocusSession A lambda function injected to trigger a formal study session loop on a specific node ID.
 * @property addRelation A lambda function injected to create bidirectional connections between two specific node IDs.
 */
class StudentCommands(
    private val repository: AppRepository,
    private val scope: CoroutineScope,
    private val currentTags: () -> List<TagEntity>,
    private val addNodeForResult: suspend (String, String, String, Long?, Long?, Boolean?) -> Long,
    private val startFocusSession: (Long) -> Unit,
    private val addRelation: (Long, Long, String) -> Unit,
) {
    /**
     * Initializes a formal focus/study session tracked against a specific academic node.
     *
     * @param nodeId The unique ID of the node being studied.
     */
    fun startStudySession(nodeId: Long) {
        startFocusSession(nodeId)
    }

    /**
     * Updates the embedded completion percentage for an academic reading material.
     *
     * Modifies the underlying JSON `StudentMetadata` structure tied to the node, ensuring the
     * value remains safely clamped between 0 and 100.
     *
     * @param node The [NodeEntity] representing the reading material.
     * @param progressPercent The integer completion percentage to apply (0-100).
     */
    fun setReadingProgress(
        node: NodeEntity,
        progressPercent: Int,
    ) {
        updateStudentMetadata(node) { student ->
            student.copy(readingProgressPercent = progressPercent.coerceIn(0, 100))
        }
    }

    /**
     * Updates the self-assessed mastery percentage of a specific academic topic or concept.
     *
     * Modifies the underlying JSON `StudentMetadata` structure tied to the node, ensuring the
     * value remains safely clamped between 0 and 100.
     *
     * @param node The [NodeEntity] representing the academic topic.
     * @param topic The optional topic identifier or concept name whose masteryPercent is being set; may be null.
     * @param masteryPercent The integer mastery percentage to apply (0-100).
     */
    fun setTopicMastery(
        node: NodeEntity,
        topic: String?,
        masteryPercent: Int,
    ) {
        updateStudentMetadata(node) { student ->
            student.copy(
                topic = topic?.trim()?.ifBlank { null },
                masteryPercent = masteryPercent.coerceIn(0, 100),
            )
        }
    }

    /**
     * Updates an academic node with formal course registration details.
     * This modifies the internal `StudentMetadata` embedded in the node's JSON structure.
     *
     * @param node The [NodeEntity] being updated.
     * @param courseId The formal code for the course (e.g., 'CS101').
     * @param courseName The human-readable name of the course.
     * @param semester The semester or term identifier (e.g., 'Fall 2026').
     * @param assignmentType An optional classification if this node represents coursework.
     */
    fun setStudentCourse(
        node: NodeEntity,
        courseId: String?,
        courseName: String?,
        semester: String?,
        assignmentType: String? = null,
    ) {
        updateStudentMetadata(node) { student ->
            student.copy(
                courseId = courseId?.trim()?.ifBlank { null },
                courseName = courseName?.trim()?.ifBlank { null },
                semester = semester?.trim()?.ifBlank { null },
                assignmentType = assignmentType?.trim()?.ifBlank { null } ?: student.assignmentType,
            )
        }
    }

    /**
     * Creates a new general note specifically pre-populated with academic context.
     * It wraps the created node with `StudentMetadata` to track course and topic assignments.
     *
     * @param title The title of the note.
     * @param content The body text of the note.
     * @param noteType The categorization (e.g., 'lecture_note', 'reading_summary').
     * @param courseId The optional course identifier.
     * @param courseName The optional full course name.
     * @param semester The optional semester context.
     * @param topic The optional subject matter topic.
     */
    fun addStudentNote(
        title: String,
        content: String,
        noteType: String,
        courseId: String? = null,
        courseName: String? = null,
        semester: String? = null,
        topic: String? = null,
    ) {
        scope.launch {
            val id = addNodeForResult(title, content, "note", null, null, false)
            val node = repository.getNodeById(id) ?: return@launch
            val envelope =
                NodeMetadataEnvelope(
                    student =
                        StudentMetadata(
                            courseId = courseId?.trim()?.ifBlank { null },
                            courseName = courseName?.trim()?.ifBlank { null },
                            semester = semester?.trim()?.ifBlank { null },
                            topic = topic?.trim()?.ifBlank { null },
                        ),
                )
            repository.updateNode(
                node.copy(
                    noteType = noteType,
                    metadataJson = node.withMetadataEnvelope(envelope).metadataJson,
                    updatedAt = Clock.System.now().toEpochMilliseconds(),
                ),
            )
        }
    }

    fun toggleFlashcardCandidate(
        node: NodeEntity,
        enabled: Boolean,
    ) {
        scope.launch {
            setTagOnNode(node.id, "flashcard_candidate", enabled)
            updateStudentMetadataInternal(node) { student ->
                student.copy(flashcardCandidate = enabled)
            }
        }
    }

    fun toggleRevisitBeforeExam(
        node: NodeEntity,
        enabled: Boolean,
    ) {
        scope.launch {
            setTagOnNode(node.id, "revisit_before_exam", enabled)
            updateStudentMetadataInternal(node) { student ->
                student.copy(revisitBeforeExam = enabled)
            }
        }
    }

    fun linkTopicToNote(
        topicNodeId: Long,
        noteNodeId: Long,
    ) {
        addRelation(topicNodeId, noteNodeId, "TOPIC_LINK")
    }

    /**
     * Establishes a formal "PAPER_REFERENCE" relation between a research paper node and a standard note.
     * Used for structuring literature reviews or connecting citations to insights.
     */
    fun linkPaperToNote(
        paperNodeId: Long,
        noteNodeId: Long,
    ) {
        addRelation(paperNodeId, noteNodeId, "PAPER_REFERENCE")
    }

    /**
     * Helper method to deserialize, safely mutate, and re-serialize `StudentMetadata` embedded
     * within the JSON column of a `NodeEntity`.
     *
     * @param node The [NodeEntity] whose metadata is being mutated.
     * @param update A lambda providing the existing [StudentMetadata] (or default) and returning the mutated version.
     */
    private fun updateStudentMetadata(
        node: NodeEntity,
        update: (StudentMetadata) -> StudentMetadata,
    ) {
        scope.launch {
            updateStudentMetadataInternal(node, update)
        }
    }

    private suspend fun updateStudentMetadataInternal(
        node: NodeEntity,
        update: (StudentMetadata) -> StudentMetadata,
    ) {
        val envelope = node.metadataEnvelopeOrNull() ?: NodeMetadataEnvelope()
        val current = envelope.student ?: StudentMetadata()
        val updated = node.withMetadataEnvelope(envelope.copy(student = update(current)))
        repository.updateNode(updated.copy(updatedAt = Clock.System.now().toEpochMilliseconds()))
    }

    private suspend fun setTagOnNode(
        nodeId: Long,
        tagName: String,
        enabled: Boolean,
    ) {
        val normalized = tagName.trim().lowercase()
        val existingTag = currentTags().firstOrNull { it.normalizedName == normalized }
        val tagId =
            existingTag?.id
                ?: repository.insertTag(
                    TagEntity(
                        name = tagName.trim(),
                        normalizedName = normalized,
                    ),
                )
        if (enabled) {
            repository.attachTagToNode(nodeId, tagId)
        } else {
            repository.detachTagFromNode(nodeId, tagId)
        }
    }
}
