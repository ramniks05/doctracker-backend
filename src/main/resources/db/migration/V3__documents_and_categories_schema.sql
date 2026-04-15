-- Core schema for categories and documents.

CREATE TABLE IF NOT EXISTS categories (
  id BIGSERIAL PRIMARY KEY,
  name VARCHAR(120) NOT NULL
);

-- Ensure category name unique (matches JPA unique constraint)
DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM pg_constraint WHERE conname = 'uk_categories_name'
  ) THEN
    ALTER TABLE categories ADD CONSTRAINT uk_categories_name UNIQUE (name);
  END IF;
END $$;

CREATE TABLE IF NOT EXISTS documents (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT NOT NULL,
  category_id BIGINT NOT NULL,
  name VARCHAR(200) NOT NULL,
  brand_name VARCHAR(200),
  purchase_date DATE,
  expiry_date DATE,
  notes VARCHAR(2000),
  image_url VARCHAR(1000),
  status VARCHAR(30) NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Foreign keys (best-effort add)
DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM pg_constraint WHERE conname = 'fk_documents_user'
  ) THEN
    ALTER TABLE documents
      ADD CONSTRAINT fk_documents_user
      FOREIGN KEY (user_id) REFERENCES users(id);
  END IF;

  IF NOT EXISTS (
    SELECT 1 FROM pg_constraint WHERE conname = 'fk_documents_category'
  ) THEN
    ALTER TABLE documents
      ADD CONSTRAINT fk_documents_category
      FOREIGN KEY (category_id) REFERENCES categories(id);
  END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_documents_user_id ON documents(user_id);
CREATE INDEX IF NOT EXISTS idx_documents_category_id ON documents(category_id);
CREATE INDEX IF NOT EXISTS idx_documents_expiry_date ON documents(expiry_date);

