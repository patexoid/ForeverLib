package com.patex.zombie.core.storage.service;

import com.google.common.base.Supplier;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.patex.LibException;
import com.patex.zombie.core.storage.model.FileData;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class DataHandler {

    private static final Pattern DUPLICATE_FILENAME_PATTERN = Pattern.compile("([^\\\\.]+?)(?:_(\\d+))?\\.(.+)");
    private final FileStorage fileStorage;
    private LoadingCache<String, ReentrantLock> lockStorage = CacheBuilder.newBuilder().
            expireAfterAccess(Duration.ofMinutes(5)).
            build(CacheLoader.from((Supplier<ReentrantLock>) ReentrantLock::new));

    @Autowired
    public DataHandler(FileStorage fileStorage) {
        this.fileStorage = fileStorage;
    }

    DataHandler(LoadingCache<String, ReentrantLock> lockStorage, FileStorage fileStorage) {
        this.lockStorage = lockStorage;
        this.fileStorage = fileStorage;
    }


    public String save(MultipartFile file, String bucket) throws LibException {
        try {
            return save(bucket, file.getOriginalFilename(), file.getInputStream());
        } catch (IOException e) {
            throw new LibException(e.getMessage(), e);
        }
    }

    private String save(String bucket, String fileName, InputStream inputStream) throws LibException {

        ReentrantLock lock = lockStorage.getUnchecked(fileName);

        lock.lock();
        try {
            if (fileStorage.exists(bucket, fileName)) {
                Matcher matcher = DUPLICATE_FILENAME_PATTERN.matcher(fileName);
                if (matcher.matches()) {
                    String prefix = matcher.group(1);
                    String suffix = matcher.group(2);
                    String extension = matcher.group(3);
                    if (suffix != null) {
                        fileName = prefix + "_" + (Integer.parseInt(suffix) + 1) + "." + extension;
                    } else {
                        fileName = prefix + "_1." + extension;
                    }
                    return save(bucket, fileName, inputStream);
                } else {
                    throw new LibException("Can't match file name " + fileName
                            + " pattern " + DUPLICATE_FILENAME_PATTERN.pattern());
                }
            }
            return fileStorage.save(inputStream, bucket, fileName);
        } finally {
            lock.unlock();
        }
    }

    @SneakyThrows
    public Optional<FileData> load(String bucket, String fileId) throws LibException {
        return fileStorage.load(bucket, fileId);
    }
}
