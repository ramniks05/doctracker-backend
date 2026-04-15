-- Add OCR/warranty fields to documents.

ALTER TABLE documents
  ADD COLUMN IF NOT EXISTS warranty_months INTEGER;

ALTER TABLE documents
  ADD COLUMN IF NOT EXISTS ocr_raw_text VARCHAR(10000);

CREATE INDEX IF NOT EXISTS idx_documents_status ON documents(status);

