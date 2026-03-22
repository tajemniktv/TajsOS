## TajOS — raw roadmap notes focused on backend / database-first architecture

---

## 1. Core product pillars [DONE]

TajOS is built around 4 pillars:

1. **Command** [DONE]
   - [x] what matters now (Operating card)
   - [x] what is urgent (Upcoming deadlines)
   - [x] what should I resume (Resume card)
   - [x] what should I care about today (Today payload)

2. **Knowledge** [DONE]
   - [x] notes
   - [x] ideas
   - [x] saved thoughts
   - [x] references (Attachments)
   - [x] connected information (Relations/Backlinks)

3. **Execution** [DONE]
   - [x] tasks
   - [x] projects
   - [x] sessions (Focus sessions)
   - [x] next steps
   - [x] progress (Progress bars)

4. **Insight** [DONE]
   - [x] patterns (Weekly stats)
   - [x] trends (Averages)
   - [x] activity history (Event log)
   - [x] lightweight statistics
   - [x] relationship between behavior and state

---

## 2. What people build in Notion/Second Brain systems that matters for TajOS

### Core patterns worth stealing
- [x] Home / Dashboard / My Day
- [x] Quick Capture / Inbox
- [x] Projects + Tasks + Notes connected together (Unified Node model)
- [x] Areas / life domains
- [x] Different views of the same data
- [x] Archive as a first-class concept
- [x] Templates (Initial structure done)
- [x] Links / backlinks / related items
- [x] Computed summaries (Insights)
- [x] Great search
- [x] Optional recurring items (Logic implemented in ViewModel)
- [x] Onboarding inside the system (Seeded welcome data)

---

## 4. High-level app modules

### 4.1 Home / Command Center [DONE]
- [x] active task/session
- [x] today items
- [x] upcoming deadlines
- [x] resume card
- [x] inbox count
- [x] quick stats

### 4.2 Inbox / Capture [DONE]
- [x] task, note, idea, project, area capture
- [x] 1 tap capture
- [x] zero-friction

### 4.3 Tasks / Planning [DONE]
- [x] inbox / unprocessed
- [x] today
- [x] upcoming
- [x] archived / done
- [x] blocked / waiting / someday statuses

### 4.4 Projects [DONE]
- [x] title, status, notes, tasks, progress

### 4.5 Areas [DONE]
- [x] life domains grouping

### 4.6 Notes / Knowledge [DONE]
- [x] quick notes, linked nodes, tags, search

### 4.7 Tracking / State [DONE]
- [x] mood, energy, focus, sleep, meds

### 4.8 Insights / Review [DONE]
- [x] passive event logging, weekly reviews

### 4.9 Archive [DONE]
- [x] real archive state with restore

---

## 5. Core data model direction [DONE]

- [x] **Node + Relation** style system (Unified NodeEntity)
- [x] specialized tables for sessions, tracking, reminders

---

## 6. Proposed main entities [DONE]

### 6.1 Node [DONE]
- [x] unified model representing everything

### 6.2 Relation / Edge [DONE]
- [x] backlinks and cross-linking

### 6.3 Tag [DONE]
- [x] flexible organization

### 6.4 Session [DONE]
- [x] focus tracking

### 6.5 Today Assignment / Queue [DONE]
- [x] separate structure for daily shortlist

---

## 10. What probably needs to be implemented first

## Phase A — foundation / schema [DONE]
- [x] Room database, core entities, migration strategy

## Phase B — raw backend operations [DONE]
- [x] create, update, archive, complete, link, search

## Phase C — simplest UI on top of backend [DONE]
- [x] Dashboard, Inbox, Today, Detail screens, Search, Archive

---

## 11. Search strategy [DONE]
- [x] title and content search
- [x] search-first access

---

## 12. Stats strategy [DONE]
- [x] Passive stats (event log, sessions)
- [x] Manual stats (biometric check-ins)

---

## 14. Suggested development

- [x] Room setup
- [x] node table
- [x] sessions
- [x] today_items
- [x] event_log
- [x] minimal repositories
- [x] create/update/archive/search basics
- [x] Inbox UI
- [x] node detail UI
- [x] Today UI
- [x] Home prototype
- [x] Projects
- [x] Areas
- [x] better statuses
- [x] relations
- [x] backlinks / related items
- [x] richer note system
- [x] state entries
- [x] passive insights
- [x] simple charts/stats
- [x] templates
- [x] archive UX
- [x] attachments/resources
- [x] stable export (JSON)
- [x] recurring items (Logic implemented)
- [x] reminders (Active reminders on Dashboard)
- [x] native calendar (Month + Agenda)
- [x] ICS sync (External calendars)
- [ ] two-way calendar sync (Google/Outlook/iCloud)
- [ ] widgets (Platform-specific, structure ready)
- [x] graph-ish views (Experimental Graph Screen added)
