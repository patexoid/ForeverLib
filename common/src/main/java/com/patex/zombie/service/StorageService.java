package com.patex.zombie.service;

import com.patex.zombie.LibException;

import java.io.InputStream;

public interface StorageService {
    String save(byte[] file, String... filepath) throws LibException;

    InputStream load(String fileId) throws LibException;
}
