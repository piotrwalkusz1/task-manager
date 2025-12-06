-- Make end_time required - all sessions in DB must be completed
-- With memory-based architecture, active sessions are stored in memory only

-- Remove constraint that allowed NULL end_time
DROP INDEX IF EXISTS idx_active_session;
DROP TRIGGER IF EXISTS enforce_single_active_session;

-- Recreate table with NOT NULL constraint on end_time
CREATE TABLE work_session_new (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    task_id INTEGER NOT NULL,
    start_time TEXT NOT NULL,
    end_time TEXT NOT NULL,
    FOREIGN KEY (task_id) REFERENCES task(id) ON DELETE CASCADE
);

-- Copy data (V4 migration already deleted incomplete sessions)
INSERT INTO work_session_new (id, task_id, start_time, end_time)
SELECT id, task_id, start_time, end_time
FROM work_session
WHERE end_time IS NOT NULL;

-- Replace old table
DROP TABLE work_session;
ALTER TABLE work_session_new RENAME TO work_session;
