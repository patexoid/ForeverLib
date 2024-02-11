package com.patex.forever.model;

import java.io.Serializable;

public class BookImage implements Serializable {

    private byte[] image;

    private String type;

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
