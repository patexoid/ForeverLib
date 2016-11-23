package com.patex.storage;

import com.patex.LibException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

@Service("tempStorage")
public class TempFileStorage implements FileStorage {
    private Path tempDirectory;

    @PostConstruct
    public void postContruct() throws IOException {
        tempDirectory = Files.createTempDirectory("zombieLibTemp");
    }

    @Override
    public String getType() {
        return "local";
    }


    @Override
    public String save(String fileName, byte[] fileContent) throws LibException{
        String filePath = tempDirectory.toAbsolutePath() + File.separator + fileName;
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
        String filePath = tempDirectory.toAbsolutePath() + File.separator + fileId;
        try {
            return new FileInputStream(filePath);
        } catch (FileNotFoundException e) {
            throw new LibException(e);
        }
    }

    @PreDestroy
    public void after(){
        tempDirectory.toFile().delete();
    }
}
