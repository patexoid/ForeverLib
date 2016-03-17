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

    public InputStream load(String fileId) throws LibException{
        try {
            return new FileInputStream(fileId);
        } catch (FileNotFoundException e) {
            throw new LibException(e);
        }
    }
}
