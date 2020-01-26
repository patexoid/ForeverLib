package com.patex.zombie.core.storage.service.mock;

import com.patex.zombie.core.storage.service.LocalFileStorage;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;

@Service
@Primary
public class TempFileStorage extends LocalFileStorage {

    public TempFileStorage() throws IOException {
        super(Files.createTempDirectory("zombie").toString());
    }
}
