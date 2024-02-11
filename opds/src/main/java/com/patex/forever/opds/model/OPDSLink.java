package com.patex.forever.opds.model;

/**
 * Created by Alexey on 07.05.2017.
 */
public class OPDSLink {

    public static final String OPDS_CATALOG = "application/atom+xml;profile=opds-catalog";
    public static final String FB2_ZIP = "application/fb2+zip";
    public static final String FB2 = "application/fb2";

    private final String href;
    private final String rel;
    private final String type;

    public OPDSLink(String href, String type) {
        this(href, null,type);

    }

    public OPDSLink(String href, String rel, String type) {
        this.href = href;
        this.rel = rel;
        this.type = type;
    }

    public String getHref() {
        return href;
    }

    public String getRel() {
        return rel;
    }

    public String getType() {
        return type;
    }

}
