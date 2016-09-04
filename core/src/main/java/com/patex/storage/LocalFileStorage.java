package com.patex.storage;

import com.patex.LibException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;

import java.io.*;

@Service
@PropertySource("/application.properties")
@Profile("fileStorage")
public class LocalFileStorage implements FileStorage {


    @Value("${localStorage.folder}")
    public  String storageFolder;

    @Override
    public String getType() {
        return "local";
    }


    @Override
    public String save(String fileName, byte[] fileContent) throws LibException{
        String filePath = storageFolder + File.separator + fileName;
        try(FileOutputStream fos = new FileOutputStream(filePath)){
            fos.write(fileContent);
            fos.flush();
        } catch (IOException e) {
            throw new LibException(e);
        }
        return filePath;
    }

    @Override
    public InputStream load(String fileId) throws LibException{
        try {
            return new FileInputStream(fileId);
        } catch (FileNotFoundException e) {
            throw new LibException(e);
        }
    }
}
