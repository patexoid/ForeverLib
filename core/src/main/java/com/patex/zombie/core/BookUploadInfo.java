package com.patex.zombie.core;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by Alexey on 7/23/2016.
 */
public class BookUploadInfo {

    private final long id;
    private final String fileName;
    private final Status status;

    public enum Status {
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
