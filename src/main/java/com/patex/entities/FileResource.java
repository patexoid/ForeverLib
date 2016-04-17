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
    long id;

    @Column
    String filePath;

    @Column
    boolean draft;

    public FileResource() {
    }

    public FileResource(String filePath) {
        this.filePath = filePath;
        this.draft = true;
    }

    public boolean isDraft() {
        return draft;
    }

    public void setDraft(boolean draft) {
        this.draft = draft;
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
}
