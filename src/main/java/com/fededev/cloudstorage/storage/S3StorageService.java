package com.fededev.cloudstorage.storage;

import com.fededev.cloudstorage.common.exception.AppException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import com.fededev.cloudstorage.common.exception.ErrorCode;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.InputStream;
import java.time.Duration;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class S3StorageService implements StorageService {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

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

    @Override
    public void deleteFiles(List<String> keys) {
        if (keys == null || keys.isEmpty()) {
            return;
        }

        List<ObjectIdentifier> objects = keys.stream()
                .map(key -> ObjectIdentifier.builder().key(key).build())
                .toList();

        DeleteObjectsRequest request = DeleteObjectsRequest.builder()
                .bucket(bucketName)
                .delete(Delete.builder().objects(objects).build())
                .build();

        this.s3Client.deleteObjects(request);

        log.info("S3 delete batch ejecutado: {} archivos", keys.size());
    }

    public String generateUrl(String S3Key, String name, String extension){
        String ext = extension.startsWith(".") ? extension : "." + extension;
        String fullFileName = name + ext;

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(this.bucketName)
                .key(S3Key)
                .responseContentDisposition("attachment; filename=\"" + fullFileName + "\"")
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(5))
                .getObjectRequest(getObjectRequest)
                .build();

        PresignedGetObjectRequest presignedRequest = this.s3Presigner.presignGetObject(presignRequest);

        return presignedRequest.url().toString();
    }
}
