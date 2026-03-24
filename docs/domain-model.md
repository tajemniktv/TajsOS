# TajsOS Domain Model

TajsOS uses a unified, highly relational domain model built around the concept of "Nodes". This allows the system to treat tasks, notes, projects, and high-level life management concepts as first-class citizens using a single source of truth.

## Core Entity: `NodeEntity`

The heart of TajsOS is the `NodeEntity` (table: `nodes`). Almost everything the user creates is a node.

Instead of having separate tables for tasks, notes, or projects, `NodeEntity` acts as a unified model. This simplifies relations, tagging, and global searching.

### Node Attributes:

*   **`type`**: Determines *what* the node is.
    *   *Standard types*: `task`, `note`, `project`, `area`, `resource`, `idea`.
    *   *LifeOS types*: `open_loop`, `decision`, `maintenance`, `protocol`, `person`, etc.
*   **`status`**: Determines the *lifecycle state* of the node.
    *   *Examples*: `active`, `done`, `archived`, `on_hold`, `someday`, `blocked`.
    *   *Important*: Nodes marked as `archived` are explicitly excluded from standard queries (e.g., `NodeDao.getAllNodesWithPins()`).
*   **Metadata**: Nodes contain rich metadata such as `energy` levels, `friction` ratings, and due dates, which the system uses for insights and filtering.

## Relationships & Hierarchies

Because all items are nodes, linking a task to a project or an idea to a resource is handled through a generic relation system.

*   **`RelationEntity`**: Represents a directed graph edge between two nodes. For example, linking a child `task` node to a parent `project` node. This allows for deep nesting and infinite hierarchies.
*   **`TagEntity` & `NodeTagEntity`**: Nodes can have multiple tags, joined through the `NodeTagEntity` junction table.

## Auxiliary Entities

These entities augment the core nodes with specific "Life OS" functionalities.

*   **`TodayPinEntity`**: Represents items pinned to the "Today" view. This is a separate table rather than a column on the `NodeEntity` so that items can be pinned or unpinned without mutating the core node. The `NodeWithPin` data class combines these two via Room relations.
*   **`FocusSessionEntity`**: Stores completed Pomodoro-style focus sessions linked to specific nodes. Used heavily by the insights engine.
*   **`TrackEntryEntity`**: Represents daily activity or progress tracking, separate from standard node completion.
*   **`ReviewEntity`**: Represents formal reflection sessions (daily, weekly, monthly reviews).
*   **`TemplateEntity`**: Pre-defined structures for creating new nodes quickly (e.g., a standard structure for a new "project" node).
*   **`NodeSnapshotEntity`**: Stores historical versions or backups of node content for version control or auditing.
*   **`EventLogEntity`**: System-level audit logs, primarily designed for debugging and future sync reconciliation.

## External Integrations

*   **`CalendarProviderEntity` & `CalendarEventEntity`**: Represents external calendar sources (Google, Outlook, ICS) and caches their events locally so they can be viewed alongside internal TajsOS nodes in the Agenda/Today views.

## The Room Database

The entire domain model is persisted using Room (via a Kotlin Multiplatform implementation). The database (`AppDatabase.kt`) coordinates all these entities.

**Important Data Layer Rule:** We prefer direct database queries (DAO methods with `WHERE` clauses) over fetching entire flows of data and filtering them in-memory within the ViewModel. This ensures performance remains high even as the "second brain" scales to thousands of nodes.
