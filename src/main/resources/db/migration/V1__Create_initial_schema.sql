-- Create task table
CREATE TABLE task (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    queue_order INTEGER NOT NULL,
    created_at TEXT NOT NULL
);

CREATE UNIQUE INDEX idx_task_queue_order ON task(queue_order);

-- Create work_session table
CREATE TABLE work_session (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    task_id INTEGER NOT NULL,
    start_time TEXT NOT NULL,
    end_time TEXT,
    FOREIGN KEY (task_id) REFERENCES task(id) ON DELETE CASCADE
);

CREATE UNIQUE INDEX idx_active_session ON work_session(task_id)
    WHERE end_time IS NULL;

-- Trigger to enforce single active session globally
CREATE TRIGGER enforce_single_active_session
BEFORE INSERT ON work_session
WHEN NEW.end_time IS NULL
BEGIN
    SELECT RAISE(ABORT, 'Only one active work session allowed')
    WHERE EXISTS (
        SELECT 1 FROM work_session WHERE end_time IS NULL
    );
END;
