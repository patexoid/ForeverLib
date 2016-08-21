package com.patex;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by Alexey on 7/23/2016.
 */
public class BookUploadInfo {

    private String fileName;
    private Status status;

    public static enum Status {
        Failed,
        Success
    }

//    public BookUploadInfo(@JsonProperty("fileName") String fileName, String status) {
//        this(fileName, Status.valueOf(status));
//    }



    public BookUploadInfo(@JsonProperty("fileName")String fileName, @JsonProperty("status") Status status) {
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
