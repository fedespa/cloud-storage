package com.fededev.cloudstorage.sharing.service;

import com.fededev.cloudstorage.common.exception.AppException;
import com.fededev.cloudstorage.common.exception.ErrorCode;
import com.fededev.cloudstorage.file.model.File;
import com.fededev.cloudstorage.file.model.response.FileWithUrlDto;
import com.fededev.cloudstorage.file.repository.FileRepository;
import com.fededev.cloudstorage.infraestructure.security.CustomUserDetails;
import com.fededev.cloudstorage.infraestructure.security.utils.HashUtils;
import com.fededev.cloudstorage.infraestructure.security.utils.TokenWithHash;
import com.fededev.cloudstorage.sharing.model.SharedLink;
import com.fededev.cloudstorage.sharing.model.response.SharedLinkDto;
import com.fededev.cloudstorage.sharing.repository.SharedLinkRepository;
import com.fededev.cloudstorage.sharing.request.CreateSharedLinkRequest;
import com.fededev.cloudstorage.storage.StorageService;
import com.fededev.cloudstorage.workspace.member.model.WorkspaceMember;
import com.fededev.cloudstorage.workspace.member.service.WorkspaceMemberService;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SharingService {

    private final FileRepository fileRepository;
    private final WorkspaceMemberService memberService;
    private final HashUtils hashUtils;
    private final SharedLinkRepository sharedLinkRepository;
    private final StorageService storageService;

    private static final Duration MIN = Duration.ofMinutes(5);
    private static final Duration MAX = Duration.ofDays(30);

    public SharedLinkDto shareFile(UUID fileId, CreateSharedLinkRequest request, CustomUserDetails user){
        File file = this.fileRepository.findActiveByIdWithWorkspace(fileId)
                .orElseThrow(() -> new AppException(ErrorCode.FILE_NOT_FOUND));

        WorkspaceMember member = this.memberService.getMemberIfIsInWorkspace(file.getWorkspace().getId(), user.getId());

        // Solo un admin o el owner del workspace pueden compartir archivos.
        if (!member.isAdminOrOwner()) {
            throw new AppException(ErrorCode.WORKSPACE_ACCESS_DENIED);
        }

        Duration duration = validateExpiration(request.durationSeconds());
        Instant expiresAt = Instant.now().plus(duration);

        TokenWithHash tokenPair = this.hashUtils.generateHashToken();

        SharedLink sharedLink = SharedLink.builder()
                .token(tokenPair.hashedToken())
                .file(file)
                .expiresAt(expiresAt)
                .build();

        SharedLink savedLink = this.sharedLinkRepository.save(sharedLink);

        return SharedLinkDto.from(
                savedLink,
                tokenPair.rawToken()
        );
    }

    public FileWithUrlDto resolve(String rawToken){
        String hashedToken = this.hashUtils.sha256(rawToken);

        SharedLink sharedLink = this.sharedLinkRepository.findValidByToken(hashedToken, Instant.now())
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_SHARE_TOKEN));

        validate(sharedLink);

        File file = sharedLink.getFile();

        if (file == null || file.getDeletedAt() != null) {
            throw new AppException(ErrorCode.FILE_DELETED);
        }

        String url = this.storageService.generateUrl(file.getS3Key(), file.getName(), file.getExtension());

        return FileWithUrlDto.fromEntity(file, url);
    }

    private void validate(SharedLink link) {
        if (link.isRevoked()) {
            throw new AppException(ErrorCode.LINK_REVOKED);
        }

        if (link.isExpired()) {
            throw new AppException(ErrorCode.LINK_EXPIRED);
        }
    }

    private Duration validateExpiration(Long seconds) {
        if (seconds == null) {
            return Duration.ofSeconds(300);
        }

        Duration duration = Duration.ofSeconds(seconds);

        if (duration.compareTo(MIN) < 0 || duration.compareTo(MAX) > 0) {
            throw new AppException(ErrorCode.INVALID_EXPIRATION_DATE);
        }

        return duration;
    }

}

