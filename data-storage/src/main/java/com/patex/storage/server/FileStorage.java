package com.patex.storage.server;

import com.google.inject.ImplementedBy;
import com.patex.LibException;

import java.nio.file.Path;

@ImplementedBy(LocalFileStorage.class)
public interface FileStorage {
    boolean exists(String bucket, String filename);

    String save(byte[] fileContent, String bucket, String filename) throws LibException;

    String getFilePath(String bucket, String filename);

    Path getPath(String bucket, String filename) throws LibException;
}
