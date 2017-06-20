package com.patex.entities;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

/**
 * Created by Alexey on 07.05.2017.
 */
@Entity
public class SavedBook {

    @Id
    @GeneratedValue
    private Long id;


    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    private ExtLibrary extLibrary;

    private String extId;

    public SavedBook() {
    }

    public SavedBook(ExtLibrary extLibrary, String extId) {
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
}
