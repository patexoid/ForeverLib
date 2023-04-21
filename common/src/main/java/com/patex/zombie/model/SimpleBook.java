package com.patex.zombie.model;

import lombok.Data;

import java.time.Instant;

@Data
public class SimpleBook {
    private Long id;
    private boolean duplicate = false;
    private String title;
    private String fileName;
    private Integer contentSize = 0;
    private Instant created;
    private byte[] checksum;
    private String descr;

    public Boolean isPrimary() {
        return !duplicate;
    }
    private FileResource fileResource;
}
