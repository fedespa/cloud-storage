package com.fededev.cloudstorage.file.service;

import com.fededev.cloudstorage.common.exception.AppException;
import com.fededev.cloudstorage.common.exception.ErrorCode;
import com.fededev.cloudstorage.file.model.File;
import com.fededev.cloudstorage.file.repository.FileRepository;
import com.fededev.cloudstorage.file.request.MoveFileRequest;
import com.fededev.cloudstorage.file.request.UploadFileRequest;
import com.fededev.cloudstorage.folder.model.Folder;
import com.fededev.cloudstorage.folder.repository.FolderRepository;
import com.fededev.cloudstorage.infraestructure.security.CustomUserDetails;
import com.fededev.cloudstorage.user.model.AppUser;
import com.fededev.cloudstorage.workspace.member.model.WorkspaceMember;
import com.fededev.cloudstorage.workspace.member.service.WorkspaceMemberService;
import com.fededev.cloudstorage.workspace.model.Workspace;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
public class FileServiceTest {
    @Mock private WorkspaceMemberService memberService;
    @Mock private FileRepository fileRepository;
    @Mock private FolderRepository folderRepository;

    @InjectMocks
    private FileService fileService;

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(fileService, "maxFileSize", 10_000_000L);
    }

    @Test
    void shouldFailWhenFileIsEmpty() {
        MultipartFile file = mock(MultipartFile.class);

        when(file.isEmpty()).thenReturn(true);

        UploadFileRequest request = new UploadFileRequest(UUID.randomUUID(), UUID.randomUUID(), file);
        CustomUserDetails user = mock(CustomUserDetails.class);

        AppException ex = assertThrows(AppException.class, () -> {
            this.fileService.uploadFile(request, user);
        });

        assertEquals(ErrorCode.FILE_IS_EMPTY.getCode(), ex.getCode());
    }

    @Test
    void shouldFailWhenExtensionIsInvalid() {
        MultipartFile file = mock(MultipartFile.class);

        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn("virus.exe");
        when(file.getSize()).thenReturn(100L);

        UploadFileRequest request = new UploadFileRequest(UUID.randomUUID(), UUID.randomUUID(), file);
        CustomUserDetails user = mock(CustomUserDetails.class);

        AppException ex = assertThrows(AppException.class, () -> {
            this.fileService.uploadFile(request, user);
        });

        assertEquals(ErrorCode.INVALID_FILE_EXTENSION.getCode(), ex.getCode());
    }

    @Test
    void shouldFailWhenFolderIsTheSame() {
        UUID fileId = UUID.randomUUID();
        UUID folderId = UUID.randomUUID();
        UUID workspaceId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Folder folder = Folder.builder()
                .id(folderId)
                .build();

        Workspace workspace = Workspace.builder()
                .id(workspaceId)
                .build();

        AppUser owner = AppUser.builder()
                .id(userId)
                .build();

        File file = File.builder()
                .id(fileId)
                .folder(folder)
                .workspace(workspace)
                .owner(owner)
                .build();

        MoveFileRequest request = new MoveFileRequest(folderId);

        CustomUserDetails user = mock(CustomUserDetails.class);
        when(user.getId()).thenReturn(userId);

        WorkspaceMember member = mock(WorkspaceMember.class);
        when(member.isAdminOrOwner()).thenReturn(true);

        when(fileRepository.findActiveByIdWithWorkspace(fileId))
                .thenReturn(Optional.of(file));

        when(memberService.getMemberIfIsInWorkspace(workspaceId, userId))
                .thenReturn(member);

        when(folderRepository.findByIdAndWorkspaceId(folderId, workspaceId))
                .thenReturn(Optional.of(folder));

        AppException ex = assertThrows(AppException.class, () -> {
            fileService.moveFile(fileId, request, user);
        });

        assertEquals(ErrorCode.FILE_ALREADY_IN_FOLDER.getCode(), ex.getCode());
    }
}
