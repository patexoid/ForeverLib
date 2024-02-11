package com.patex.forever.service;


import com.patex.forever.model.User;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.function.Predicate;
import java.util.stream.Stream;

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
                                 BookService bookService, LibUserService libUserService,
                                 ExecutorCreator executorCreator) {
        super(FileSystems.getDefault().getPath(path), bookService, libUserService,
                Executors.newSingleThreadExecutor(executorCreator.createThreadFactory("DirWatcherService", log)));
    }

    @Override
    public void setUp() {

    }

    @Scheduled(fixedDelay = 30000)
    @SneakyThrows
    public void watch() {
        Optional<User> adminUser = getAdminUser();
        if (adminUser.isEmpty()) {
            log.error("No admin User");
            return;
        }
        try (Stream<Path> walk = Files.walk(directoryPath)) {
            walk.filter(Predicate.not(path -> path.startsWith(failedDir)))
                    .forEach(p -> processPath(p, adminUser));
        }

    }

    @SneakyThrows
    private void processPath(Path p, Optional<User> adminUser) {
        assert adminUser.isPresent();
        if (Files.isDirectory(p)) { //I'm to lazy to process and delete all dirs on the same step
            try(Stream<Path> children=Files.list(p)) {
                if(children.findFirst().isEmpty()){
                    Files.delete(p);
                }
            }
        } else {
            processFile(p.toFile(), adminUser.get());
        }
    }
}
