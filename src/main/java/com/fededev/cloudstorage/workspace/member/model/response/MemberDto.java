package com.fededev.cloudstorage.workspace.member.model.response;

import com.fededev.cloudstorage.user.model.AppUser;
import com.fededev.cloudstorage.workspace.member.model.WorkspaceMember;
import com.fededev.cloudstorage.workspace.member.model.WorkspaceRole;

import java.time.Instant;
import java.util.UUID;

public record MemberDto(

        UUID userId,
        String email,
        WorkspaceRole role,
        Instant joinedAt

) {

    public static MemberDto from(WorkspaceMember member) {
        return new MemberDto(
                member.getUser().getId(),
                member.getUser().getEmail(),
                member.getRole(),
                member.getJoinedAt()
        );
    }

}
