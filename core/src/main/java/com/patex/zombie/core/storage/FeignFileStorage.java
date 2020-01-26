package com.patex.zombie.core.storage;

import com.patex.LibException;
import com.patex.jwt.JwtTokenUtil;
import com.patex.model.UploadResponse;
import com.patex.zombie.core.api.DataStorage;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static com.patex.jwt.JwtTokenUtil.FILE_DOWNLOAD;
import static com.patex.jwt.JwtTokenUtil.FILE_UPLOAD;

@Service
@Profile("feignStorage")

public class FeignFileStorage implements FileStorage {

    private final DataStorage dataStorage;
    private final JwtTokenUtil jwtTokenUtil;

    public FeignFileStorage(DataStorage dataStorage, JwtTokenUtil jwtTokenUtil) {
        this.dataStorage = dataStorage;
        this.jwtTokenUtil = jwtTokenUtil;
    }

    @Override
    public String getType() {
        return "feign";
    }

    @Override
    public String save(byte[] fileContent, String bucket, String fileName) throws LibException {
        String token = jwtTokenUtil.generateToken("core", FILE_UPLOAD);

        List<UploadResponse> upload = dataStorage.upload("Bearer " + token, bucket, new MultipartFile() {
                    @Override
                    public String getName() {
                        return "file";
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
        return upload.get(0).getId();
    }

    @Override
    public InputStream load(String bucket, String fileId) throws LibException {
        String token = jwtTokenUtil.generateToken("core", FILE_DOWNLOAD);
        try {
            return dataStorage.downloadBook("Bearer " + token, bucket, fileId).getBody().getInputStream();
        } catch (IOException e) {
            throw new LibException(e);
        }
    }

    @Override
    public boolean exists(String bucket, String fileName) {
        return false;
    }
}
