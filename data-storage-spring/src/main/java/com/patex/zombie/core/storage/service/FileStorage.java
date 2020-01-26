package com.patex.zombie.core.storage.service;

import com.patex.LibException;
import com.patex.zombie.core.storage.model.FileData;

import java.io.InputStream;
import java.util.Optional;

public interface FileStorage {
    boolean exists(String bucket, String filename);

    String save(InputStream inputStream, String bucket, String fileName) throws LibException;

    Optional<FileData> load(String bucket, String fileId) throws LibException;
}
