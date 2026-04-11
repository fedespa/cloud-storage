package com.fededev.cloudstorage.storage;

import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;

import java.util.List;
import java.util.Map;

public interface StorageService {

    void delete(String key);
    void deleteFiles(List<String> keys);
    String generateUrl(String S3Key, String name, String extension);
    Map<String, String> generatePresignedPost(String s3Key, String contentType, long maxBytes);
    HeadObjectResponse headObject(String s3Key);

    S3Client getClient();
    String getBucket();

    String promoteObject(String pendingKey);

}
