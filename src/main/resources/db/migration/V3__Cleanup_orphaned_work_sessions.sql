-- Delete orphaned work sessions (sessions for tasks that don't exist)
DELETE FROM work_session
WHERE task_id NOT IN (SELECT id FROM task);
