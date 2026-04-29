CREATE TABLE IF NOT EXISTS deletion_folder_jobs (

    id UUID PRIMARY KEY,
    folder_id UUID NOT NULL,
    requested_by UUID NOT NULL,
    status VARCHAR(50) NOT NULL,
    started_at TIMESTAMPTZ,
    attempts INTEGER NOT NULL,
    finished_at TIMESTAMPTZ,
    error_message TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()

);