/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui

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

class StudentCommands(
    private val repository: AppRepository,
    private val scope: CoroutineScope,
    private val currentTags: () -> List<TagEntity>,
    private val addNodeForResult: suspend (String, String, String, Long?, Long?, Boolean?) -> Long,
    private val startFocusSession: (Long) -> Unit,
    private val addRelation: (Long, Long, String) -> Unit,
) {
    fun startStudySession(nodeId: Long) {
        startFocusSession(nodeId)
    }

    fun setReadingProgress(
        node: NodeEntity,
        progressPercent: Int,
    ) {
        updateStudentMetadata(node) { student ->
            student.copy(readingProgressPercent = progressPercent.coerceIn(0, 100))
        }
    }

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

    fun linkPaperToNote(
        paperNodeId: Long,
        noteNodeId: Long,
    ) {
        addRelation(paperNodeId, noteNodeId, "PAPER_REFERENCE")
    }

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
