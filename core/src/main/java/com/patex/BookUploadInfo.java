package com.patex;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by Alexey on 7/23/2016.
 */
public class BookUploadInfo {

    private long id;
    private String fileName;
    private Status status;

    public static enum Status {
        Failed,
        Success
    }


    public BookUploadInfo(@JsonProperty("id") long id,
                          @JsonProperty("fileName") String fileName,
                          @JsonProperty("status") Status status) {
        this.id = id;
        this.fileName = fileName;
        this.status = status;
    }

    public long getId() {
        return id;
    }

    public String getFileName() {
        return fileName;
    }

    public Status getStatus() {
        return status;
    }


}
