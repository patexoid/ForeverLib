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
    private FileStorage localFileStorage;

    public String save(String fileName, byte[] file) throws LibException{
        return localFileStorage.save(fileName, file);
    }

    public InputStream load(String fileId) throws LibException{
        return localFileStorage.load(fileId);
    }

}
