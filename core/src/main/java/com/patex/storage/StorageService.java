package com.patex.storage;

import com.patex.LibException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.InputStream;
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

    public String save(byte[] file, String bucket, String fileName) throws LibException {

        if (fileStorage.exists(bucket, fileName)) {
            Matcher matcher = DUPLICATE_FILENAME_PATTERN.matcher(fileName);
            if (matcher.matches()) {
                String prefix = matcher.group(1);
                String suffix = matcher.group(2);
                String extension = matcher.group(3);
                if (suffix != null) {
                    fileName = prefix + "_" + (Integer.parseInt(suffix) + 1) + "_." + extension;
                } else {
                    fileName = prefix + "_1_." + extension;
                }
                return save(file, bucket, fileName);
            } else {
                throw new LibException("Can't match file name " + fileName
                        + " pattern " + DUPLICATE_FILENAME_PATTERN.pattern());
            }
        }
        return fileStorage.save(file, bucket, fileName);
    }

    public InputStream load(String fileId) throws LibException {
        return fileStorage.load(fileId);
    }

}
