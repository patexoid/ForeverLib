package com.patex.storage;

import com.patex.LibException;
import com.patex.api.DataStorage;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

@Service
@Profile("feignStorage")
public class FeignFileStorage implements FileStorage {

    private final DataStorage dataStorage;


    public FeignFileStorage(DataStorage dataStorage) {
        this.dataStorage = dataStorage;
    }

    @Override
    public String getType() {
        return "feign";
    }

    @Override
    public String save(byte[] fileContent, String bucket, String fileName) throws LibException {
        return dataStorage.upload(bucket, new MultipartFile() {
            @Override
            public String getName() {
                return fileName;
            }

            @Override
            public String getOriginalFilename() {
                return fileName;
            }

            @Override
            public String getContentType() {
                return null;
            }

            @Override
            public boolean isEmpty() {
                return false;
            }

            @Override
            public long getSize() {
                return fileContent.length;
            }

            @Override
            public byte[] getBytes() throws IOException {
                return fileContent;
            }

            @Override
            public InputStream getInputStream() throws IOException {
                return new ByteArrayInputStream(fileContent);
            }

            @Override
            public void transferTo(File dest) throws IOException, IllegalStateException {
                new FileOutputStream(dest).write(fileContent);
            }
        });
    }

    @Override
    public InputStream load(String fileId) throws LibException {
        return null;
    }

    @Override
    public boolean exists(String bucket, String fileName) {
        return false;
    }
}
