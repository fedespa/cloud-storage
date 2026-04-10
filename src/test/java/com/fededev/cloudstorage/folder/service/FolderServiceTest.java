package com.fededev.cloudstorage.folder.service;

import com.fededev.cloudstorage.common.exception.AppException;
import com.fededev.cloudstorage.common.exception.ErrorCode;
import com.fededev.cloudstorage.folder.model.Folder;
import com.fededev.cloudstorage.folder.repository.FolderRepository;
import com.fededev.cloudstorage.folder.request.MoveFolderRequest;
import com.fededev.cloudstorage.infraestructure.security.CustomUserDetails;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FolderServiceTest {

    @Mock private FolderRepository folderRepository;
    @Mock private WorkspaceMemberService memberService;

    @InjectMocks
    private FolderService folderService;

    @Test
    void shouldFailWhenDestinationFolderIsSubFolder() {

        UUID folderToMoveId = UUID.randomUUID();
        UUID destinationId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID workspaceId = UUID.randomUUID();

        Workspace workspace = createWorkspace(workspaceId);
        Folder folderToMove = createFolder(folderToMoveId, workspace);
        Folder destinationFolder = createFolder(destinationId, workspace);

        CustomUserDetails user = mock(CustomUserDetails.class);
        when(user.getId()).thenReturn(userId);

        when(folderRepository.findActiveByIdWithWorkspace(folderToMoveId))
                .thenReturn(Optional.of(folderToMove));

        when(folderRepository.findActiveByIdWithWorkspace(destinationId))
                .thenReturn(Optional.of(destinationFolder));

        WorkspaceMember member = mock(WorkspaceMember.class);
        when(member.isAdminOrOwner()).thenReturn(true);

        when(memberService.getMemberIfIsInWorkspace(workspaceId, userId))
                .thenReturn(member);

        when(folderRepository.isDescendant(destinationId, folderToMoveId))
                .thenReturn(1L);

        AppException ex = assertThrows(AppException.class, () -> {
            this.folderService.move(
                    folderToMoveId,
                    new MoveFolderRequest(destinationId),
                    user

            );
        });

        assertEquals(ErrorCode.CANNOT_MOVE_INTO_SUBFOLDER.getCode(), ex.getCode());
        verify(folderRepository, never()).save(any());
    }

    private Workspace createWorkspace(UUID id) {
        return Workspace.builder()
                .id(id)
                .build();
    }

    private Folder createFolder(UUID id, Workspace workspace) {
        return Folder.builder()
                .id(id)
                .workspace(workspace)
                .build();
    }

}
