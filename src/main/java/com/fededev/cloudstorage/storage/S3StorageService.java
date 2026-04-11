package com.fededev.cloudstorage.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class S3StorageService implements StorageService {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Value("${aws.s3.bucket}")
    private String bucket;

    @Override
    public void delete(String key) {
        try {
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(this.bucket)
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
                .bucket(bucket)
                .delete(Delete.builder().objects(objects).build())
                .build();

        this.s3Client.deleteObjects(request);

        log.info("S3 delete batch ejecutado: {} archivos", keys.size());
    }

    public String generateUrl(String S3Key, String name, String extension){
        String ext = extension.startsWith(".") ? extension : "." + extension;
        String fullFileName = name + ext;

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(this.bucket)
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

    public String promoteObject(String pendingKey) {
        String permanentKey = pendingKey.replace("workspaces/", "files/");

        this.s3Client.copyObject(r -> r
                .sourceBucket(bucket)
                .sourceKey(pendingKey)
                .destinationBucket(bucket)
                .destinationKey(permanentKey)
        );

        this.s3Client.deleteObject(r -> r
                .bucket(bucket)
                .key(pendingKey)
        );

        return permanentKey;
    }

    @Override
    public Map<String, String> generatePresignedPost(String s3Key, String contentType, long maxBytes) {
        AwsCredentials credentials = DefaultCredentialsProvider.create().resolveCredentials();
        String region = this.s3Client.serviceClientConfiguration().region().id();

        Instant now = Instant.now();
        String date     = DateTimeFormatter.ofPattern("yyyyMMdd")
                .withZone(ZoneOffset.UTC).format(now);
        String dateTime = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'")
                .withZone(ZoneOffset.UTC).format(now);
        String expiration = DateTimeFormatter.ISO_INSTANT
                .format(now.plus(Duration.ofMinutes(10)));

        String credentialScope = date + "/" + region + "/s3/aws4_request";
        String credential      = credentials.accessKeyId() + "/" + credentialScope;

        String policyJson = """
            {
              "expiration": "%s",
              "conditions": [
                {"bucket": "%s"},
                {"key": "%s"},
                {"Content-Type": "%s"},
                {"x-amz-credential": "%s"},
                {"x-amz-algorithm": "AWS4-HMAC-SHA256"},
                {"x-amz-date": "%s"},
                ["content-length-range", 1, %d]
              ]
            }
            """.formatted(expiration, bucket, s3Key, contentType,
                credential, dateTime, maxBytes);

        String policyB64 = Base64.getEncoder()
                .encodeToString(policyJson.getBytes(StandardCharsets.UTF_8));

        String signature = signPolicyV4(policyB64, credentials.secretAccessKey(), date, region);

        Map<String, String> fields = new LinkedHashMap<>();
        fields.put("key",               s3Key);
        fields.put("Content-Type",      contentType);
        fields.put("x-amz-credential",  credential);
        fields.put("x-amz-algorithm",   "AWS4-HMAC-SHA256");
        fields.put("x-amz-date",        dateTime);
        fields.put("policy",            policyB64);
        fields.put("x-amz-signature",   signature);
        fields.put("upload_url",        "https://" + bucket + ".s3." + region + ".amazonaws.com/");

        return fields;
    }

    public HeadObjectResponse headObject(String s3Key) {
        try {
            return this.s3Client.headObject(
                    HeadObjectRequest.builder().bucket(bucket).key(s3Key).build()
            );
        } catch (S3Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Archivo no encontrado en S3");
        }
    }

    @Override
    public S3Client getClient() {
        return this.s3Client;
    }

    @Override
    public String getBucket() {
        return this.bucket;
    }

    private String signPolicyV4(String policyB64, String secretKey, String date, String region) {
        try {
            byte[] signingKey = buildSigningKey(secretKey, date, region);
            return HexFormat.of().formatHex(hmacSHA256(signingKey, policyB64));
        } catch (Exception e) {
            throw new RuntimeException("Error firmando policy S3", e);
        }
    }

    private byte[] buildSigningKey(String secretKey, String date, String region) throws Exception {
        byte[] kDate    = hmacSHA256(("AWS4" + secretKey).getBytes(StandardCharsets.UTF_8), date);
        byte[] kRegion  = hmacSHA256(kDate,    region);
        byte[] kService = hmacSHA256(kRegion,  "s3");
        return            hmacSHA256(kService, "aws4_request");
    }

    private byte[] hmacSHA256(byte[] key, String data) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(key, "HmacSHA256"));
        return mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
    }
}
