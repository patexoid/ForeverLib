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
public class TempFileStorage extends LocalFileStorage {

    public TempFileStorage() throws IOException{
        super(Files.createTempDirectory("zombieLibTemp").toString());
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
