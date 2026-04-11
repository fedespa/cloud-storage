package com.fededev.cloudstorage.file.service;

import com.fededev.cloudstorage.TestDataFactory;
import com.fededev.cloudstorage.common.exception.AppException;
import com.fededev.cloudstorage.common.exception.ErrorCode;
import com.fededev.cloudstorage.file.model.File;
import com.fededev.cloudstorage.file.repository.FileRepository;
import com.fededev.cloudstorage.file.request.MoveFileRequest;
import com.fededev.cloudstorage.folder.model.Folder;
import com.fededev.cloudstorage.infraestructure.security.CustomUserDetails;
import com.fededev.cloudstorage.user.model.AppUser;
import com.fededev.cloudstorage.workspace.member.model.WorkspaceMember;
import com.fededev.cloudstorage.workspace.member.service.WorkspaceMemberService;
import com.fededev.cloudstorage.workspace.model.Workspace;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static io.lettuce.core.KillArgs.Builder.id;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class FileServiceTest {

    @Mock private WorkspaceMemberService memberService;
    @Mock private FileRepository fileRepository;

    @InjectMocks
    private FileService fileService;

    @Test
    void shouldFailMoveFileWhenFolderIsTheSame() {
        UUID fileId = UUID.randomUUID();
        UUID folderId = UUID.randomUUID();
        UUID workspaceId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();


        Workspace workspace = TestDataFactory.createWorkspace(workspaceId);

        Folder folder = TestDataFactory.createFolder(folderId, workspace);
        AppUser owner = TestDataFactory.createUser(userId,null,null);
        File file = TestDataFactory.createFile(fileId, folder, workspace, owner,null);

        MoveFileRequest request = new MoveFileRequest(folderId);

        CustomUserDetails user = mock(CustomUserDetails.class);
        when(user.getId()).thenReturn(userId);

        WorkspaceMember member = mock(WorkspaceMember.class);
        when(member.isAdminOrOwner()).thenReturn(true);

        when(fileRepository.findActiveByIdWithWorkspace(fileId))
                .thenReturn(Optional.of(file));

        when(memberService.getMemberIfIsInWorkspace(workspaceId, userId))
                .thenReturn(member);

        AppException ex = assertThrows(AppException.class, () -> {
            fileService.moveFile(fileId, request, user);
        });

        assertEquals(ErrorCode.FILE_ALREADY_IN_FOLDER.getCode(), ex.getCode());
    }

}
