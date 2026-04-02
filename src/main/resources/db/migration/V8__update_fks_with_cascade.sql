ALTER TABLE folders
    DROP CONSTRAINT IF EXISTS fk_folders_parent;

ALTER TABLE folders
    ADD CONSTRAINT fk_folders_parent
    FOREIGN KEY (parent_id)
    REFERENCES folders(id)
    ON DELETE CASCADE;