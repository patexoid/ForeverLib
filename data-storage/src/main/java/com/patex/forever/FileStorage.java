package com.patex.forever;

import java.io.InputStream;

/**
 * Created by Alexey on 8/15/2016.
 */
public interface FileStorage {
    String getType();

    String save(byte[] fileContent, String... filePath) throws LibException;

    InputStream load(String fileId) throws LibException;

    boolean exists(String... filePath);

    String move(String oldPath, String[] newPath) throws LibException;
}
