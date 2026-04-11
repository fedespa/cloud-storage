CREATE TABLE IF NOT EXISTS files (

    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    extension VARCHAR(10) NOT NULL,
    folder_id UUID,
    owner_id UUID NOT NULL,
    workspace_id UUID NOT NULL,
    size BIGINT NOT NULL,
    s3_key VARCHAR(255) NOT NULL,
    mime_type VARCHAR(100) NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMPTZ,
    confirmed_at TIMESTAMPTZ,

    CONSTRAINT fk_files_folder
        FOREIGN KEY (folder_id)
        REFERENCES folders(id),
    CONSTRAINT fk_files_owner
        FOREIGN KEY (owner_id)
        REFERENCES users(id),
    CONSTRAINT fk_files_workspace
        FOREIGN KEY (workspace_id)
        REFERENCES workspaces(id)

);

CREATE UNIQUE INDEX unique_file_active_in_root
    ON files (workspace_id, name, extension)
    WHERE folder_id IS NULL AND deleted_at IS NULL;

CREATE UNIQUE INDEX unique_file_active_in_folder
    ON files (workspace_id, folder_id, name, extension)
    WHERE folder_id IS NOT NULL AND deleted_at IS NULL;

CREATE INDEX idx_files_by_folder
    ON files (workspace_id, folder_id)
    WHERE deleted_at IS NULL;

CREATE INDEX idx_files_by_extension
    ON files (extension)
    WHERE deleted_at IS NULL;