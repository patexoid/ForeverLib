package com.patex.zombie;

import com.patex.zombie.LibException;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
@Profile("tempStorage")
public class TempFileStorage implements FileStorage {
    private Path tempDirectory;

    @PostConstruct
    public void postConstruct() throws IOException {
        tempDirectory = Files.createTempDirectory("zombieLibTemp");
    }

    @Override
    public String getType() {
        return "local";
    }


    @Override
    public boolean exists(String... filepath) {
        return new File(getFilePath(filepath)).exists();
    }

    @Override
    public String save(byte[] fileContent, String... filepath) throws LibException {
        File file = new File(getFilePath(filepath));
        File dir = file.getParentFile();
        if(!dir.exists()){
            dir.mkdirs();
        }
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(fileContent);
            fos.flush();
        } catch (IOException e) {
            throw new LibException(e);
        }
        return file.getAbsolutePath();
    }

    @Override
    public InputStream load(String fileId) throws LibException {
        try {
            return new FileInputStream(fileId);
        } catch (FileNotFoundException e) {
            throw new LibException(e);
        }
    }


    private String getFilePath(String... fileName) {
        return tempDirectory.toAbsolutePath() + File.separator + String.join(File.separator,fileName);
    }

    @PreDestroy
    public void after() {
        tempDirectory.toFile().delete();
    }
}
