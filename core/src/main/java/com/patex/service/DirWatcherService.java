package com.patex.service;

import com.patex.zombie.service.BookService;
import com.patex.zombie.service.ExecutorCreator;
import com.patex.zombie.model.User;
import com.patex.zombie.service.UserService;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

@Component
@ConditionalOnExpression("!'${localStorage.bulk-upload.folder}'.isEmpty()")
@Profile("!docker")
public class DirWatcherService {
    private static final Logger log = LoggerFactory.getLogger(DirWatcherService.class);
    public static final String FAILED_DIRECTORY = "failed";

    protected final Path directoryPath;
    private final BookService bookService;
    private final ZUserService zUserService;
    private final Executor executor;

    protected volatile boolean running = false;

    @Autowired
    public DirWatcherService(@Value("${localStorage.bulk-upload.folder}") String path,
                             BookService bookService, ZUserService zUserService,
                             ExecutorCreator executorCreator) {
        this(FileSystems.getDefault().getPath(path), bookService, zUserService,
                Executors.newSingleThreadExecutor(executorCreator.createThreadFactory("DirWatcherService", log)));
    }

    public DirWatcherService(Path directoryPath, BookService bookService,
                             ZUserService zUserService, Executor executor) {
        this.directoryPath = directoryPath;
        this.bookService = bookService;
        this.zUserService = zUserService;
        this.executor = executor;
    }

    @PostConstruct
    public void setUp() {
        Optional<User> user = getAdminUser();
        user.ifPresent(zUser -> run());
    }

    private synchronized void run() {
        if (!running) {
            running = true;
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
            if (!Files.exists(directoryPath)) {
                Files.createDirectories(directoryPath);
            }
            WatchService watchService = directoryPath.getFileSystem().newWatchService();
            directoryPath.register(watchService, StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_DELETE);
            while (running) {
                WatchKey watchKey = watchService.take();
                Optional<User> user = getAdminUser();
                assert user.isPresent();
                watchKey.pollEvents().stream().
                        filter(e -> StandardWatchEventKinds.ENTRY_CREATE.equals(e.kind())).
                        map(e -> (Path) e.context()).
                        map(directoryPath::resolve).
                        map(Path::toFile).
                        filter(File::exists).
                        forEach(file -> processFile(file, user.get()));
            }
        } catch (InterruptedException e) {
            log.error("Thread got interrupted:" + e.getMessage());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    private void initStart() {
        Optional<User> user = getAdminUser();
        user.ifPresent(zUser -> {
            File dir = directoryPath.toFile();
            for (File file : Objects.requireNonNull(dir.listFiles())) {
                processFile(file, zUser);
            }
        });
    }

    protected Optional<User> getAdminUser() {
        return zUserService.getByRole(UserService.ADMIN_AUTHORITY).stream().findFirst();
    }

    @SneakyThrows
    protected void processFile(File file, User adminUser) {
        if (file.getName().toLowerCase().endsWith(".zip")) {
            try (ZipInputStream zip = new ZipInputStream(new FileInputStream(file))) {
                ZipEntry entry;
                while ((entry = zip.getNextEntry()) != null) {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    ZipOutputStream zop = new ZipOutputStream(baos);
                    zop.setLevel(Deflater.BEST_COMPRESSION);
                    zop.putNextEntry(new ZipEntry(entry));
                    zip.transferTo(zop);
                    zop.flush();
                    zop.close();
                    String fileName = entry.getName() + ".zip";
                    byte[] newFileContent = baos.toByteArray();
                    try {
                        bookService.uploadBook(fileName, new ByteArrayInputStream(newFileContent), adminUser);
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                        File parent = file.getParentFile();
                        File failedDir = new File(parent, FAILED_DIRECTORY);
                        if (!failedDir.exists()) {
                            failedDir.mkdir();
                        }
                        try (FileOutputStream fos= new FileOutputStream(new File(failedDir, fileName))) {
                            fos.write(newFileContent);
                        }
                    }
                }
            }
        } else {
            try (FileInputStream fis = new FileInputStream(file)) {
                bookService.uploadBook(file.getName(), fis, adminUser);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                return;
            }
        }
        //noinspection ResultOfMethodCallIgnored
        file.delete();
    }
}
