package com.fededev.cloudstorage;

import com.fededev.cloudstorage.file.model.File;
import com.fededev.cloudstorage.file.model.FileStatus;
import com.fededev.cloudstorage.folder.model.Folder;
import com.fededev.cloudstorage.user.model.AppUser;
import com.fededev.cloudstorage.workspace.model.Workspace;

import java.util.UUID;

public class TestDataFactory {

    public static AppUser createUser(UUID id, String email, String password) {
        return AppUser.builder()
                .id(id)
                .email(email != null ? email : "defaulttest@gmail.com")
                .password(password != null ? password : "defaultpassword")
                .build();
    }

    public static Workspace createWorkspace(UUID id) {
        return Workspace.builder()
                .id(id)
                .name("Mi Workspace")
                .build();
    }

    public static Folder createFolder(UUID id, Workspace workspace) {
        return Folder.builder()
                .id(id)
                .name("Carpeta de Prueba")
                .workspace(workspace)
                .build();
    }

    public static File createFile(UUID id, Folder folder, Workspace workspace, AppUser owner, String ext) {
        return File.builder()
                .id(id)
                .name("archivo")
                .extension(ext != null ? ext : "txt")
                .folder(folder)
                .workspace(workspace)
                .owner(owner)
                .status(FileStatus.UPLOADED)
                .build();
    }
}
