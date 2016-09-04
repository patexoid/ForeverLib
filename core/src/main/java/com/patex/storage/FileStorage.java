package com.patex.storage;

import com.patex.LibException;

import java.io.InputStream;

/**
 * Created by Alexey on 8/15/2016.
 */
public interface FileStorage {
    String getType();

    String save(String fileName, byte[] fileContent) throws LibException;

    InputStream load(String fileId) throws LibException;
}
