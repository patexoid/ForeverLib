package com.patex.entities;

import javax.persistence.*;

/**
 * Created by Alexey on 07.05.2017.
 */
@Entity
public class SavedBook {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int failedDownloadCount;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    private ExtLibrary extLibrary;

    private String extId;

    public SavedBook() {
    }

    public SavedBook(ExtLibrary extLibrary, String extId) {
        this.extLibrary = extLibrary;
        this.extId = extId;
    }

    public SavedBook(ExtLibrary extLibrary, String extId, int failedDownloadCount) {
        this.failedDownloadCount = failedDownloadCount;
        this.extLibrary = extLibrary;
        this.extId = extId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


    public ExtLibrary getExtLibrary() {
        return extLibrary;
    }

    public void setExtLibrary(ExtLibrary extLibrary) {
        this.extLibrary = extLibrary;
    }

    public String getExtId() {
        return extId;
    }

    public void setExtId(String extId) {
        this.extId = extId;
    }

    public int getFailedDownloadCount() {
        return failedDownloadCount;
    }

    public void setFailedDownloadCount(int failedDownloadCount) {
        this.failedDownloadCount = failedDownloadCount;
    }

    public void failed() {
        failedDownloadCount++;
    }

    public void success() {
        failedDownloadCount = 0;
    }
}
