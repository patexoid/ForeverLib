package com.patex.storage;

import com.patex.LibException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@PropertySource("/application.properties")
@Profile("fileStorage")
public class LocalFileStorage implements FileStorage {


    public static final Pattern DUPLICATE_FILENAME_PATTERN = Pattern.compile("(.*_)(\\d+)");
    @Value("${localStorage.folder}")
    public  String storageFolder;

    @Override
    public String getType() {
        return "local";
    }


    @Override
    public String save(String fileName, byte[] fileContent) throws LibException{
        String filePath = storageFolder + File.separator + fileName;
        if(new File(filePath).exists()){
            Matcher matcher = DUPLICATE_FILENAME_PATTERN.matcher(fileName);
            if(matcher.matches()){
                String prefix =matcher.group(1);
                String suffix =matcher.group(2);
                save(prefix+Integer.parseInt(suffix), fileContent);

            } else {
                return save(fileName+"_1", fileContent);
            }
        }
        try(FileOutputStream fos = new FileOutputStream(filePath)){
            fos.write(fileContent);
            fos.flush();
        } catch (IOException e) {
            throw new LibException(e);
        }
        return filePath;
    }

    @Override
    public InputStream load(String fileId) throws LibException{
        try {
            return new FileInputStream(fileId);
        } catch (FileNotFoundException e) {
            throw new LibException(e);
        }
    }
}
