package com.patex.forever;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import jakarta.annotation.PreDestroy;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

@Service

@Profile("tempStorage")
public class TempFileStorage extends LocalFileStorage {

    public TempFileStorage() throws IOException{
        super(Files.createTempDirectory("foreverLibTemp").toString());
    }

    @Override
    public String getType() {
        return "local";
    }



    @PreDestroy
    public void after() throws IOException {
            Files.delete(Path.of(storageFolder));
    }
}
