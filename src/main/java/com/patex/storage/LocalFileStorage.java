package com.patex.storage;

import com.patex.LibException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;

import java.io.*;

@Service
@PropertySource("/application.properties")
public class LocalFileStorage {


    @Value("${localStorage.folder}")
    public  String storageFolder;

    public String getType() {
        return "local";
    }

    public String save(String fileName, InputStream is) throws LibException{
        byte[] buffer = new byte[32768];
        String filePath = storageFolder + File.separator + fileName;
        try(FileOutputStream fos = new FileOutputStream(filePath)){
            while (true) {
                int readBytesCount = is.read(buffer);
                if (readBytesCount == -1) {
                    break;
                }
                if (readBytesCount > 0) {
                    fos.write(buffer, 0, readBytesCount);
                }
            }
            fos.flush();
        } catch (IOException e) {
            throw new LibException(e);
        }
        return filePath;
    }

    public InputStream load(String fileId) {
        return null;
    }
}
