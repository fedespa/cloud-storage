package com.fededev.cloudstorage.file.service;

import com.fededev.cloudstorage.common.exception.AppException;
import com.fededev.cloudstorage.common.exception.ErrorCode;
import com.fededev.cloudstorage.file.request.UploadFileRequest;
import com.fededev.cloudstorage.infraestructure.security.CustomUserDetails;
import com.fededev.cloudstorage.infraestructure.validator.FileTypeValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
public class UploadFileServiceTest {

    @Mock private FileTypeValidator validator;

    @InjectMocks
    private UploadFileService uploadFileService;

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(uploadFileService, "maxSizeBytes", 10_000_000L);
    }

    @Test
    void shouldFailInitiateUploadWhenExtensionIsInvalid() {
        UploadFileRequest request = new UploadFileRequest("archivo.exe", "application/vnd.microsoft.portable-executable", 10000L);
        CustomUserDetails user = mock(CustomUserDetails.class);

        doThrow(new AppException(ErrorCode.INVALID_FILE_TYPE)).when(validator)
                .validateMimeType(anyString());

        AppException ex = assertThrows(AppException.class, () -> {
            this.uploadFileService.initiateUpload(UUID.randomUUID(), UUID.randomUUID(), request, user);
        });

        assertEquals(ErrorCode.INVALID_FILE_TYPE.getCode(), ex.getCode());
    }

}
