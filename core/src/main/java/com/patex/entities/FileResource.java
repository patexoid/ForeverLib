package com.patex.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

/**
 * Created by potekhio on 17-Mar-16.
 */
@Entity
public class FileResource {

    @Id
    @GeneratedValue
    private long id;

    @Column
    private String filePath;

    @Column
    private String type;

    @Column
    private Integer size;


    public FileResource() {
    }

    public FileResource(String filePath, String type, Integer size) {
        this.filePath = filePath;
        this.type = type;
        this.size = size;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
