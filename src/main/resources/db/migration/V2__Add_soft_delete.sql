-- Add soft delete flag to task table
ALTER TABLE task ADD COLUMN is_deleted INTEGER DEFAULT 0 NOT NULL;
