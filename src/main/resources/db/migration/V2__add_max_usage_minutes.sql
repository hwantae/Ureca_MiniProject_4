ALTER TABLE room
    ADD max_usage_minutes INT NOT NULL DEFAULT 120;


UPDATE room
SET max_usage_minutes = 120
WHERE max_usage_minutes IS NULL;