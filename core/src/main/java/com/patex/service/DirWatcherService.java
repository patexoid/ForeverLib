package com.patex.service;

import com.patex.entities.ZUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileInputStream;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Component
@ConditionalOnExpression("!'${bulkUploadDir}'.isEmpty()")
public class DirWatcherService {
   private static final Logger log = LoggerFactory.getLogger(DirWatcherService.class);
    private final Path directoryPath;
    private final BookService bookService;
    private final ZUserService zUserService;
    private final Executor executor = Executors.newSingleThreadExecutor(r -> {
        Thread thread = new Thread(r);
        thread.setName("DirWatcherService");
        thread.setDaemon(true);
        thread.setUncaughtExceptionHandler((t, e) -> log.error(e.getMessage(), e));
        return thread;
    });

    private boolean isRunning = false;

    @Autowired
    public DirWatcherService(@Value("${bulkUploadDir}") String path,
                             BookService bookService, ZUserService zUserService) {
        directoryPath = FileSystems.getDefault().getPath(path);
        this.bookService = bookService;
        this.zUserService = zUserService;
    }

    @PostConstruct
    public void setUp() {
        Optional<ZUser> user = getAdminUser();
        user.ifPresent(zUser -> run());
    }

    private synchronized void run() {
        if (!isRunning) {
            isRunning = true;
            executor.execute(this::initStart);
            executor.execute(this::watch);
        }
    }

    @EventListener
    public void newUserCreated(UserCreationEvent event) {
        if (event.isAdmin()) {
            run();
        }
    }

    private void watch() {
        try {
            WatchService watchService = directoryPath.getFileSystem().newWatchService();
            directoryPath.register(watchService, StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_DELETE);
            while (true) {
                WatchKey watchKey = watchService.take();
                for (final WatchEvent<?> event : watchKey.pollEvents()) {
                    takeActionOnChangeEvent(event);
                }
            }

        } catch (InterruptedException interruptedException) {
            System.out.println("Thread got interrupted:" + interruptedException);
        } catch (Exception exception) {
            exception.printStackTrace();
        }

    }

    private void takeActionOnChangeEvent(WatchEvent<?> event) {

        Kind<?> kind = event.kind();

        if (kind.equals(StandardWatchEventKinds.ENTRY_CREATE)) {
            Path entry = (Path) event.context();
            Optional<ZUser> user = getAdminUser();
            File file = directoryPath.resolve(entry).toFile();
            if (file.exists()) {
                user.ifPresent(zUser -> processFile(file, zUser));
            }
        }
    }

    private void initStart() {
        Optional<ZUser> user = getAdminUser();
        user.ifPresent(zUser -> {
            File dir = directoryPath.toFile();
            for (File file : Objects.requireNonNull(dir.listFiles())) {
                processFile(file, zUser);
            }
        });
    }

    private Optional<ZUser> getAdminUser() {
        return zUserService.getByRole(ZUserService.ADMIN_AUTHORITY).stream().findFirst();
    }

    private void processFile(File file, ZUser adminUser) {
            try (FileInputStream fis = new FileInputStream(file)) {
                bookService.uploadBook(file.getName(), fis, adminUser);
            }  catch (Exception e) {
            log.error(e.getMessage(), e);
            return;
        }
        file.delete();
    }

}
