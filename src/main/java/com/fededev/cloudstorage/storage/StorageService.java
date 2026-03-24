package com.fededev.cloudstorage.storage;

import java.io.InputStream;

public interface StorageService {

    void upload(String key, InputStream inputStream, String contentType, long size);
    void delete(String key);

}
