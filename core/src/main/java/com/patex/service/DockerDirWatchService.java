package com.patex.service;


import com.patex.zombie.model.User;
import com.patex.zombie.service.BookService;
import com.patex.zombie.service.ExecutorCreator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.FileSystems;
import java.util.Optional;
import java.util.concurrent.Executors;

/**
 * Temporary solution java atch service not working fine on docker
 */
@Service
@ConditionalOnExpression("!'${localStorage.bulk-upload.folder}'.isEmpty()")
@Profile("docker")
@Slf4j
public class DockerDirWatchService extends DirWatcherService {


    @Autowired
    public DockerDirWatchService(@Value("${localStorage.bulk-upload.folder}") String path,
                                 BookService bookService, ZUserService zUserService,
                                 ExecutorCreator executorCreator) {
        super(FileSystems.getDefault().getPath(path), bookService, zUserService,
                Executors.newSingleThreadExecutor(executorCreator.createThreadFactory("DirWatcherService", log)));
    }

    @Override
    public void setUp() {

    }

    @Scheduled(fixedDelay = 30000)
    public void watch() {
        Optional<User> adminUser = getAdminUser();
        if (adminUser.isEmpty()) {
            log.error("No admin User");
            return;
        }
        File[] files = directoryPath.toFile().listFiles();
        if (files != null) {
            for (File file : files) {
                processFile(file, adminUser.get());
            }
        }
    }
}
