-- Support up to 2 image URLs per document.
ALTER TABLE documents
  ADD COLUMN IF NOT EXISTS image_url2 VARCHAR(1000);

