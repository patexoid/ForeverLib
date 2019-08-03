package com.patex.storage.service;

import com.patex.LibException;
import com.patex.storage.model.FileData;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

@Service
@Slf4j
public class LocalFileStorage implements FileStorage {

    private final String storageFolder;

    public LocalFileStorage(@Value("${localStorage.folder}") String storageFolder) {
        this.storageFolder = storageFolder;
    }

    @Override
    public boolean exists(String bucket, String filename) {
        return new File(getFilePath(bucket, filename)).exists();
    }

    @Override
    public String save(InputStream inputStream, String bucket, String fileName) throws LibException {
        String filePath = getFilePath(bucket, fileName);
        File file = new File(filePath);
        try {
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            FileOutputStream out = new FileOutputStream(file);
            inputStream.transferTo(out);
        } catch (IOException e) {
            throw new LibException(e.getMessage(), e);
        }
        return file.getName();
    }

    private String getFilePath(String bucket, String filename) {
        return storageFolder + File.separator + bucket + File.separator + filename;
    }

    @Override
    @SneakyThrows
    public Optional<FileData> load(String bucket, String fileId) throws LibException {
        String filePath = getFilePath(bucket, fileId);
        File file = new File(filePath);
        if (file.exists()) {
            return Optional.of(new FileData(new FileInputStream(file), file.length()));
        } else {
            return Optional.empty();
        }
    }
}

