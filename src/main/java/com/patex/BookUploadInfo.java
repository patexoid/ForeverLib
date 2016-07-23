package com.patex;

/**
 * Created by Alexey on 7/23/2016.
 */
public class BookUploadInfo {

    private final String fileName;
    private final Status status;

    public static enum Status {
        Failed,
        Success
    }

    public BookUploadInfo(String fileName, Status status) {
        this.fileName = fileName;
        this.status = status;
    }

    public String getFileName() {
        return fileName;
    }

    public Status getStatus() {
        return status;
    }
}
