package com.patex.zombie;

import com.patex.zombie.service.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 */
@Service
public class StorageServiceImpl implements StorageService {

    private static final Pattern DUPLICATE_FILENAME_PATTERN = Pattern.compile("([^\\\\.]+?)(?:_(\\d+)_)?\\.(.+)");

    private final FileStorage fileStorage;

    private static final int LEVEL = 3;

    @Autowired
    public StorageServiceImpl(FileStorage fileStorage) {
        this.fileStorage = fileStorage;
    }

    @Override
    public String save(byte[] file, boolean updatePath, String... filepath) throws LibException {
        String[] updatedPath;
        if (updatePath) {
            updatedPath = updateWithLevel(filepath);
        } else {
            updatedPath = filepath;
        }
        if (fileStorage.exists(updatedPath)) {
            Matcher matcher = DUPLICATE_FILENAME_PATTERN.matcher(filepath[filepath.length - 1]);
            if (matcher.matches()) {
                String prefix = matcher.group(1);
                String suffix = matcher.group(2);
                String extension = matcher.group(3);
                String[] newFilepath = Arrays.copyOf(filepath, filepath.length);
                if (suffix != null) {
                    newFilepath[newFilepath.length - 1] = prefix + "_" + (Integer.parseInt(suffix) + 1) + "_." + extension;
                } else {
                    newFilepath[newFilepath.length - 1] = prefix + "_1_." + extension;
                }
                return save(file, updatePath, newFilepath);
            } else {
                throw new LibException("Can't match file name " + Arrays.toString(filepath)
                        + " pattern " + DUPLICATE_FILENAME_PATTERN.pattern());
            }
        }
        return fileStorage.save(file, updatedPath);
    }

    private String[] updateWithLevel(String[] filepath) {
        String[] newFilePath = new String[filepath.length + LEVEL];
        for (int i = 0; i < LEVEL; i++) {
            if (filepath[0].length() <= i) {
                newFilePath[i] = "_";
            } else {
                newFilePath[i] = filepath[0].substring(i, i + 1);
            }
        }
        System.arraycopy(filepath, 0, newFilePath, LEVEL, filepath.length);
        return newFilePath;
    }

    @Override
    public InputStream load(String fileId) throws LibException {
        return fileStorage.load(fileId);
    }

    public String move(String oldPath, String[] newPath, boolean updatePath) throws LibException {
        String[] updatedNewPath;
        if(updatePath){
            updatedNewPath = updateWithLevel(newPath);
        } else {
            updatedNewPath=newPath;
        }
        return fileStorage.move(oldPath, updatedNewPath);
    }
}
