package com.patex.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class FileResource {

    public FileResource(String filePath, String type, Integer size) {
        this.filePath = filePath;
        this.type = type;
        this.size = size;
    }

    private long id;

    private String filePath;

    private String type;

    private Integer size;

}
