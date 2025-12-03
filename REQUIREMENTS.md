# Task Manager - Requirements Documentation

## Overview

A desktop task management application inspired by operating system thread scheduling. The application presents tasks one at a time from a queue, allowing users to work on tasks without decision paralysis.

## Technology Stack

- **Language**: Java 21
- **UI Framework**: JavaFX
- **Persistence**: Hibernate with SQLite
- **Build Tool**: Maven
- **Additional Libraries**: Lombok

## Business Requirements

### Core Problem
Users often have many simultaneous tasks and struggle with:
- Deciding which task to focus on
- Remembering all pending tasks
- Wasting time and energy on prioritization
- Risk of neglecting (starving) some tasks

### Solution Approach
Application acts as a task scheduler, similar to OS thread scheduling:
- User registers tasks to a queue
- Application presents one task at a time
- User works on tasks in rotation, similar to time-slicing
- Prevents task starvation through round-robin approach

## Functional Requirements

### 1. Task Registration
- User can create a new task
- Task contains: name (priority may be added in future)
- Task is added to the end of the queue

### 2. Task Queue Visibility
- User sees only the current task (head of queue)
- User sees the total number of tasks in queue
- User does not see other tasks in the queue

### 3. Work Session Control
- User can start work on current task
- User can pause work on current task
- Single button toggles between start/pause states
- System records all start and stop timestamps

### 4. Task Rotation
- User can move current task to end of queue
- If task is in progress, it is automatically paused first
- Next task in queue becomes visible
- Implements round-robin task switching

### 5. Time Tracking
- System records timestamp for each work session start
- System records timestamp for each work session stop
- System calculates daily time spent on task (sum for current day)
- System calculates total time spent on task (sum across all days)
- Display format: `Daily time (Total time)`

## Data Model (SQLite)

```sql
CREATE TABLE task (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    queue_order INTEGER NOT NULL,
    created_at TEXT NOT NULL
);

CREATE UNIQUE INDEX idx_task_queue_order ON task(queue_order);

CREATE TABLE work_session (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    task_id INTEGER NOT NULL,
    start_time TEXT NOT NULL,
    end_time TEXT,
    FOREIGN KEY (task_id) REFERENCES task(id) ON DELETE CASCADE
);

CREATE UNIQUE INDEX idx_active_session ON work_session(task_id)
    WHERE end_time IS NULL;

CREATE TRIGGER enforce_single_active_session
BEFORE INSERT ON work_session
WHEN NEW.end_time IS NULL
BEGIN
    SELECT RAISE(ABORT, 'Only one active work session allowed')
    WHERE EXISTS (
        SELECT 1 FROM work_session WHERE end_time IS NULL
    );
END;
```

### Design Rationale

#### Queue Management
- `queue_order` is a monotonically increasing integer
- Current task = task with lowest `queue_order`
- Adding task: assign `MAX(queue_order) + 1`
- Rotating task: update to `MAX(queue_order) + 1`
- No need to update multiple rows - O(1) operation

#### Preventing Impossible States
- **Single active session**: Unique index on `(task_id, end_time IS NULL)` prevents multiple active sessions for same task
- **Single global active session**: Trigger prevents inserting new active session when one exists
- **No status field needed**: Status is derived from data:
  - `IN_PROGRESS` = has WorkSession with `end_time IS NULL`
  - `PAUSED` = has WorkSession history but none active
  - `QUEUED` = has no WorkSession records

#### Data Types
- SQLite stores timestamps as TEXT in ISO-8601 format
- `queue_order` as INTEGER for efficient ordering
- Cascade delete ensures no orphaned work sessions

## Business Rules

### Queue Management
1. New tasks assigned `queue_order = MAX(queue_order) + 1`
2. Current task = `SELECT * FROM task ORDER BY queue_order LIMIT 1`
3. Rotating task: `UPDATE task SET queue_order = (SELECT MAX(queue_order) FROM task) + 1 WHERE id = ?`

### Work Session Management
1. Only one active work session allowed (enforced by trigger)
2. Starting work:
   - Insert new WorkSession: `INSERT INTO work_session (task_id, start_time) VALUES (?, datetime('now'))`
3. Pausing work:
   - Update current session: `UPDATE work_session SET end_time = datetime('now') WHERE task_id = ? AND end_time IS NULL`
4. Rotating task:
   - If active session exists, pause it first
   - Update queue_order to end of queue

### Task Status Derivation
```sql
-- Check if task is IN_PROGRESS
SELECT EXISTS(
    SELECT 1 FROM work_session
    WHERE task_id = ? AND end_time IS NULL
);

-- Check if task is PAUSED (has history but not active)
SELECT EXISTS(
    SELECT 1 FROM work_session WHERE task_id = ?
) AND NOT EXISTS(
    SELECT 1 FROM work_session WHERE task_id = ? AND end_time IS NULL
);

-- QUEUED = no work sessions at all
```

### Time Calculation
```sql
-- Daily time for task
SELECT SUM(
    CASE
        WHEN end_time IS NULL
        THEN (julianday('now') - julianday(start_time)) * 86400
        ELSE (julianday(end_time) - julianday(start_time)) * 86400
    END
) AS daily_seconds
FROM work_session
WHERE task_id = ?
AND date(start_time) = date('now');

-- Total time for task
SELECT SUM(
    CASE
        WHEN end_time IS NULL
        THEN (julianday('now') - julianday(start_time)) * 86400
        ELSE (julianday(end_time) - julianday(start_time)) * 86400
    END
) AS total_seconds
FROM work_session
WHERE task_id = ?;
```

## User Interface

### Main View Layout
```
+------------------------------------------+
|  Task Manager                            |
+------------------------------------------+
|                                          |
|  Current Task:                           |
|  +------------------------------------+  |
|  | [Task Name]                        |  |
|  |                                    |  |
|  | Time today: Xh Ym (Total: Ah Bm)  |  |
|  +------------------------------------+  |
|                                          |
|  [Start/Pause]  [Next Task]             |
|                                          |
|  Tasks in queue: N                       |
|                                          |
+------------------------------------------+
|  New Task:                               |
|  +------------------------------------+  |
|  | [Text Field]                       |  |
|  +------------------------------------+  |
|  [Add Task]                              |
+------------------------------------------+
```

### UI Components

#### Current Task Display
- Large area showing current task name
- Below: time tracking display
- Format: `Time today: Xh Ym (Total: Ah Bm)`

#### Action Buttons
1. **Start/Pause Button**
   - Text changes based on whether active work session exists
   - "Start" when no active work session
   - "Pause" when active work session exists

2. **Next Task Button**
   - Moves current task to end of queue
   - Shows next task

#### Queue Counter
- Display: "Tasks in queue: N"
- N = `SELECT COUNT(*) FROM task`

#### New Task Form
- Text field for task name
- "Add Task" button
- Clears field after adding task

## Use Cases

### UC1: Add New Task
1. User enters task name in text field
2. User clicks "Add Task" button
3. System executes:
   ```sql
   INSERT INTO task (name, queue_order, created_at)
   VALUES (?, COALESCE((SELECT MAX(queue_order) FROM task), 0) + 1, datetime('now'));
   ```
4. System updates queue counter
5. System clears text field

### UC2: Start Work on Task
1. User clicks "Start" button
2. System executes:
   ```sql
   INSERT INTO work_session (task_id, start_time)
   VALUES (?, datetime('now'));
   ```
3. Button text changes to "Pause"
4. Time display begins updating

### UC3: Pause Work on Task
1. User clicks "Pause" button
2. System executes:
   ```sql
   UPDATE work_session
   SET end_time = datetime('now')
   WHERE task_id = ? AND end_time IS NULL;
   ```
3. Button text changes to "Start"
4. Time display shows final time

### UC4: Rotate to Next Task
1. User clicks "Next Task" button
2. System checks if current task has active work session
3. If active session exists, pause it (see UC3)
4. System executes:
   ```sql
   UPDATE task
   SET queue_order = (SELECT MAX(queue_order) FROM task) + 1
   WHERE id = ?;
   ```
5. System loads new current task:
   ```sql
   SELECT * FROM task ORDER BY queue_order LIMIT 1;
   ```
6. Button states reset based on new task

## System Constraints

1. `queue_order` values are unique and monotonically increasing
2. Only one task can have active work session (enforced by trigger)
3. WorkSession `end_time` must be >= `start_time` (can add CHECK constraint)
4. All timestamps stored in ISO-8601 format

## Non-Functional Requirements

### Performance
- UI must remain responsive during all operations
- Database operations should not block UI thread
- Queue operations are O(1) - no need to update multiple rows

### Usability
- Interface must be simple and distraction-free
- All actions should provide immediate visual feedback
- Time format should be human-readable

### Data Integrity
- Constraints prevent invalid states
- Foreign keys with CASCADE prevent orphaned records
- Trigger enforces single active work session globally

## Future Considerations

### Planned Features
- Priority system for tasks
- Task completion marking
- Task editing capabilities
- Statistics and analytics
