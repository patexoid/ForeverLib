package com.patex.storage;

import com.patex.LibException;
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
public class StorageService {

    private static final Pattern DUPLICATE_FILENAME_PATTERN = Pattern.compile("([^\\\\.]+?)(?:_(\\d+)_)?\\.(.+)");

    private final FileStorage fileStorage;

    @Autowired
    public StorageService(FileStorage fileStorage) {
        this.fileStorage = fileStorage;
    }

    public String save(byte[] file, String... filepath) throws LibException {

        if (fileStorage.exists(filepath)) {
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
                return save(file, newFilepath);
            } else {
                throw new LibException("Can't match file name " + Arrays.toString(filepath)
                        + " pattern " + DUPLICATE_FILENAME_PATTERN.pattern());
            }
        }
        return fileStorage.save(file, filepath);
    }

    public InputStream load(String fileId) throws LibException {
        return fileStorage.load(fileId);
    }

}
