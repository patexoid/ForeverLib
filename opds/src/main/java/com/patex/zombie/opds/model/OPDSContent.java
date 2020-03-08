package com.patex.zombie.opds.model;

import com.patex.service.Resources;

import java.util.Locale;

public class OPDSContent {

    private final String type;
    private final String value;
    private final String src;


    public OPDSContent(String value) {
        type="text/html";
        this.value = value;
        src=null;
    }

    public OPDSContent(String type, String value, String src) {
        this.type = type;
        this.value = value;
        this.src = src;
    }

    public String getType() {
        return type;
    }

    public String getValue(){
        return value;
    }

    public String getValue(Resources res, Locale locale) {
        return value;
    }

    public String getSrc() {
        return src;
    }
}
