package com.patex.storage.server;

import com.patex.LibException;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;

@Singleton
public class LocalFileStorage implements FileStorage {

    private final String storageFolder;
    private final Path storageFolderPath;

    @Inject
    public LocalFileStorage(@Named("storagepath") String storageFolder) {
        this.storageFolder = storageFolder;
        storageFolderPath = FileSystems.getDefault().getPath(storageFolder);
    }

    @Override
    public boolean exists(String bucket, String filename) {
        return new File(getFilePath(bucket, filename)).exists();
    }

    @Override
    public String save(byte[] fileContent, String bucket, String filename) throws LibException {
        File file = new File(getFilePath(bucket, filename));
        File parentDir = file.getParentFile();
        if (!parentDir.exists()) {
            parentDir.mkdirs();
        }
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(fileContent);
            fos.flush();
        } catch (IOException e) {
            throw new LibException(e);
        }
        return filename;
    }

    @Override
    public String getFilePath(String bucket, String filename) {
        return storageFolder + File.separator + bucket + File.separator + filename;
    }


    @Override
    public Path getPath(String bucket, String filename) throws LibException {
        return storageFolderPath.resolve(bucket).resolve(filename);
    }
}

