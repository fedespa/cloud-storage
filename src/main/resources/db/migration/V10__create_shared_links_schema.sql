CREATE TABLE IF NOT EXISTS shared_links (

    id UUID PRIMARY KEY,
    token VARCHAR(255) UNIQUE NOT NULL,
    file_id UUID NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    revoked BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_shared_links_file
        FOREIGN KEY (file_id)
        REFERENCES files(id)
);