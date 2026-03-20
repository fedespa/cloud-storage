CREATE TABLE IF NOT EXISTS workspace_invitations (

    id UUID PRIMARY KEY,
    email VARCHAR(100) NOT NULL,
    invited_user_id UUID,
    workspace_id UUID NOT NULL,
    invited_by UUID NOT NULL,
    role VARCHAR(25) NOT NULL,
    token VARCHAR(255) UNIQUE NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    accepted BOOLEAN NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_workspace_invitations_invited_user
        FOREIGN KEY (invited_user_id)
        REFERENCES users(id),
    CONSTRAINT fk_workspace_invitations_workspace
        FOREIGN KEY (workspace_id)
        REFERENCES workspaces(id),
    CONSTRAINT fk_workspace_invitations_invited_by
        FOREIGN KEY (invited_by)
        REFERENCES users(id)

);