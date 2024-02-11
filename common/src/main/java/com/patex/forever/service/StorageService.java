package com.patex.forever.service;

import com.patex.forever.LibException;

import java.io.InputStream;

public interface StorageService {
    String save(byte[] file, boolean updatePath, String... filepath) throws LibException;

    InputStream load(String fileId) throws LibException;

    String move(String oldPath, String[] newPath, boolean updatePath)  throws LibException;
}
