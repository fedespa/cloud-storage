package com.fededev.cloudstorage.storage;

import com.fededev.cloudstorage.common.exception.AppException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import com.fededev.cloudstorage.common.exception.ErrorCode;

import java.io.InputStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class S3StorageService implements StorageService {

    private final S3Client s3Client;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    @Override
    public void upload(String key, InputStream inputStream, String contentType, long size) {
        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(this.bucketName)
                    .key(key)
                    .contentType(contentType)
                    .build();

            this.s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(inputStream, size));
            log.info("Archivo subido exitosamente a S3: {}", key);
        } catch (S3Exception e) {
            log.error("Error de AWS al subir archivo: {}", e.awsErrorDetails().errorMessage());
            throw new AppException(ErrorCode.STORAGE_UPLOAD_ERROR);
        } catch (Exception e) {
            log.error("Error inesperado al subir a S3", e);
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public void delete(String key) {
        try {
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(this.bucketName)
                    .key(key)
                    .build();

            this.s3Client.deleteObject(deleteObjectRequest);
            log.info("Archivo eliminado de S3: {}", key);
        } catch (Exception e) {
            log.error("No se pudo eliminar el archivo de S3: {}", key, e);
        }
    }
}
