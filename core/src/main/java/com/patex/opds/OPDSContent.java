package com.patex.opds;

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

    public String getValue() {
        return value;
    }

    public String getSrc() {
        return src;
    }
}
