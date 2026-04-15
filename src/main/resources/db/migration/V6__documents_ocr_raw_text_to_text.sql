-- Store full OCR output without length cap.
ALTER TABLE documents
  ALTER COLUMN ocr_raw_text TYPE TEXT;
