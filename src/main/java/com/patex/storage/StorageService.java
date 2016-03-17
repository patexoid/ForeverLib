package com.patex.storage;

import com.patex.LibException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.InputStream;

/**
 * Created by Alexey on 12.03.2016.
 */
@Service
public class StorageService {

    @Autowired
    private LocalFileStorage localFileStorage;

    public String saveFile(String fileName, byte[] file) throws LibException{
        return localFileStorage.save(fileName, file);
    }

    public InputStream getFile(String fileId) throws LibException{
        return localFileStorage.load(fileId);
    }

}
