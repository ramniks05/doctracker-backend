-- Platform-specific minimum / latest app version config for ExpiryX force-update.

CREATE TABLE IF NOT EXISTS app_version_config (
  platform VARCHAR(20) PRIMARY KEY,
  min_build INT NOT NULL,
  latest_build INT NOT NULL,
  min_version VARCHAR(32) NOT NULL,
  latest_version VARCHAR(32) NOT NULL,
  force_update BOOLEAN NOT NULL DEFAULT FALSE,
  soft_update BOOLEAN NOT NULL DEFAULT TRUE,
  title VARCHAR(200) NOT NULL,
  message VARCHAR(500) NOT NULL,
  store_url VARCHAR(1000) NOT NULL,
  release_notes TEXT,
  updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

INSERT INTO app_version_config (
  platform, min_build, latest_build, min_version, latest_version,
  force_update, soft_update, title, message, store_url, release_notes
) VALUES
(
  'android', 1, 9, '1.0.0', '1.0.0', FALSE, TRUE,
  'Update available',
  'A new version of ExpiryX is available.',
  'https://play.google.com/store/apps/details?id=com.example.expiryx',
  'Bug fixes and improved document scan.'
),
(
  'ios', 1, 9, '1.0.0', '1.0.0', FALSE, TRUE,
  'Update available',
  'A new version of ExpiryX is available.',
  'https://apps.apple.com/app/id000000000',
  'Bug fixes and improved document scan.'
),
(
  'web', 1, 9, '1.0.0', '1.0.0', FALSE, TRUE,
  'Update available',
  'A new version of ExpiryX is available.',
  'https://expiryx.example.com',
  'Bug fixes and improved document scan.'
)
ON CONFLICT (platform) DO NOTHING;
