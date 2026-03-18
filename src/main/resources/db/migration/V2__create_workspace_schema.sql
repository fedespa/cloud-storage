CREATE TABLE IF NOT EXISTS workspaces (

    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    total_quota BIGINT NOT NULL,
    used_storage BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_used_storage_positive CHECK (used_storage >= 0),
    CONSTRAINT chk_storage_within_quota CHECK (used_storage <= total_quota)

);