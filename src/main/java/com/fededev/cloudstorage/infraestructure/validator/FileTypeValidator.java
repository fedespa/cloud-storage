package com.fededev.cloudstorage.infraestructure.validator;

import com.fededev.cloudstorage.common.exception.AppException;
import com.fededev.cloudstorage.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import static java.util.Map.entry;

@Component
public class FileTypeValidator {

    private static final Map<String, byte[]> MAGIC_BYTES = Map.ofEntries(
            entry("image/jpeg", new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF}),
            entry("image/png",  new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47}),
            entry("image/gif",  new byte[]{0x47, 0x49, 0x46, 0x38}),
            entry("image/bmp",  new byte[]{0x42, 0x4D}),
            entry("image/webp", new byte[]{0x52, 0x49, 0x46, 0x46}),
            entry("application/pdf", new byte[]{0x25, 0x50, 0x44, 0x46}),
            entry("application/zip", new byte[]{0x50, 0x4B, 0x03, 0x04}),
            entry("application/msword", new byte[]{(byte) 0xD0, (byte) 0xCF, 0x11, (byte) 0xE0, (byte) 0xA1, (byte) 0xB1, 0x1A, (byte) 0xE1}),
            entry("application/x-rar-compressed", new byte[]{0x52, 0x61, 0x72, 0x21, 0x1A, 0x07, 0x00}),
            entry("audio/mpeg", new byte[]{0x49, 0x44, 0x33})
    );

    private static final Set<String> ALLOWED_TYPES = MAGIC_BYTES.keySet();

    public void validateMimeType(String declaredMimeType) {
        if (!ALLOWED_TYPES.contains(declaredMimeType)) {
            throw new AppException(ErrorCode.INVALID_FILE_TYPE);
        }
    }

    public void validateMagicBytes(
            S3Client s3Client,
            String bucket,
            String s3Key,
            String declaredMimeType
    ) {
        byte[] magic = MAGIC_BYTES.get(declaredMimeType);
        if (magic == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tipo no soportado");
        }

        String range = "bytes=0-" + (magic.length - 1);

        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(bucket)
                .key(s3Key)
                .range(range)
                .build();

        byte[] actualBytes;
        try (ResponseInputStream<GetObjectResponse> stream = s3Client.getObject(request)) {
            actualBytes = stream.readAllBytes();
        } catch (S3Exception e) {
            if (e.statusCode() == 404) {
                throw new AppException(ErrorCode.FILE_NOT_FOUND);
            }

            throw new AppException(ErrorCode.STORAGE_UPLOAD_ERROR);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error leyendo S3");
        }

        if (!startsWith(actualBytes, magic)) {
            throw new AppException(ErrorCode.INVALID_FILE_CONTENT);
        }
    }

    private boolean startsWith(byte[] data, byte[] prefix) {
        if (data.length < prefix.length) return false;
        for (int i = 0; i < prefix.length; i++) {
            if (data[i] != prefix[i]) return false;
        }
        return true;
    }

}
