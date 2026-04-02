package com.fededev.cloudstorage.storage;

import java.io.InputStream;
import java.util.List;

public interface StorageService {

    void upload(String key, InputStream inputStream, String contentType, long size);
    void delete(String key);
    void deleteFiles(List<String> keys);
    String generateUrl(String S3Key, String name, String extension);

}
