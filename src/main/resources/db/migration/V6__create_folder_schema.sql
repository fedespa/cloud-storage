CREATE TABLE IF NOT EXISTS folders (

    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    owner_id UUID NOT NULL,
    workspace_id UUID NOT NULL,
    parent_id UUID,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMPTZ,

    CONSTRAINT fk_folders_owner
        FOREIGN KEY (owner_id)
        REFERENCES users(id),
    CONSTRAINT fk_folders_workspace
        FOREIGN KEY (workspace_id)
        REFERENCES workspaces(id),
    CONSTRAINT fk_folders_parent
        FOREIGN KEY (parent_id)
        REFERENCES folders(id)
);

CREATE UNIQUE INDEX uq_folder_name_with_parent
    ON folders (name, workspace_id, parent_id)
    WHERE parent_id IS NOT NULL AND deleted_at IS NULL;

CREATE UNIQUE INDEX uq_folder_name_root
    ON folders (name, workspace_id)
    WHERE parent_id IS NULL AND deleted_at IS NULL;

CREATE INDEX idx_folders_workspace ON folders(workspace_id);