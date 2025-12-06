-- Remove work sessions without end_time as they are no longer valid
-- With new architecture, all work sessions in DB must be completed (have end_time)
DELETE FROM work_session WHERE end_time IS NULL;
