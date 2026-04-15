CREATE TABLE IF NOT EXISTS notification_logs (
  id BIGSERIAL PRIMARY KEY,
  document_id BIGINT NOT NULL,
  channel VARCHAR(30) NOT NULL,
  reminder_type VARCHAR(30) NOT NULL,
  recipient_mobile VARCHAR(20) NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM pg_constraint WHERE conname = 'fk_notification_logs_document'
  ) THEN
    ALTER TABLE notification_logs
      ADD CONSTRAINT fk_notification_logs_document
      FOREIGN KEY (document_id) REFERENCES documents(id);
  END IF;
END $$;

DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM pg_constraint WHERE conname = 'uk_notification_logs_doc_channel_type'
  ) THEN
    ALTER TABLE notification_logs
      ADD CONSTRAINT uk_notification_logs_doc_channel_type
      UNIQUE (document_id, channel, reminder_type);
  END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_notification_logs_document_id ON notification_logs(document_id);
