package com.patex.forever;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
@Profile("fileStorage")
public class LocalFileStorage implements FileStorage {

    protected final String storageFolder;
    private final Path storageFolderPath;


    public LocalFileStorage(@Value("${localStorage.folder}") String storageFolder) {
        this.storageFolder = storageFolder;
        storageFolderPath = FileSystems.getDefault().getPath(storageFolder);
    }

    @Override
    public String getType() {
        return "local";
    }

    @Override
    public boolean exists(String... filePath) {
        return new File(getFilePath(filePath)).exists();
    }

    @Override
    public String save(byte[] fileContent, String... filePath) throws LibException {
        File file = new File(getFilePath(filePath));
        File parentDir = file.getParentFile();
        if (!parentDir.exists()) {
            parentDir.mkdirs();
        }
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(fileContent);
            fos.flush();
        } catch (IOException e) {
            throw new LibException(e);
        }
        return storageFolderPath.relativize(file.toPath()).toString();
    }

    private String getFilePath(String... fileName) {
        return storageFolder + File.separator + String.join(File.separator, fileName);
    }

    @Override
    public InputStream load(String fileId) throws LibException {
        try {
            return new FileInputStream(getFilePath(fileId));
        } catch (FileNotFoundException e) {
            throw new LibException(e);
        }
    }

    public String move(String oldPath, String[] newPath) throws LibException {
        try {
            Path oldFilePath = storageFolderPath.resolve(oldPath);

            if (Files.isRegularFile(oldFilePath)) {
                Path newAbsPath = Path.of(getFilePath(newPath));
                Files.createDirectories(newAbsPath.getParent());
                Path moved = Files.move(oldFilePath, newAbsPath);
                deleteEmpty(oldFilePath.getParent());
                return storageFolderPath.relativize(moved).toString();
            }
            throw new LibException("Cant move:" + oldPath);
        } catch (IOException e) {
            throw new LibException(e);
        }
    }

    private void deleteEmpty(Path parent) throws IOException {
        if (Files.isDirectory(parent) && Files.list(parent).findFirst().isEmpty()) {
            Files.delete(parent);
            if (parent.getParent() != null && parent.startsWith(storageFolderPath)) {
                deleteEmpty(parent);
            }
        }
    }
}
