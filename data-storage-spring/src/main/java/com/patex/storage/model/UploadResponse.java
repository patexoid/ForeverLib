package com.patex.storage.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UploadResponse {

    private Status status;
    private String id;
    private String errorMessage;

    public static UploadResponse error(String errorMessage) {
        return new UploadResponse(Status.Failed, null, errorMessage);
    }

    public static UploadResponse success(String id) {
        return new UploadResponse(Status.Success, id, null);
    }

    public enum Status {
        Failed,
        Success
    }
}
