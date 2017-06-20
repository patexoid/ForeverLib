package com.patex.extlib;

import com.patex.opds.OPDSAuthor;

/**
 * Created by Alexey on 18.06.2017.
 */
public class ExtLibAuthor implements OPDSAuthor {


    private final String authorName;
    private final String url;

    public ExtLibAuthor(String authorName, String url) {
        this.authorName = authorName;
        this.url = url;
    }

    @Override
    public String getName() {
        return authorName;
    }

    @Override
    public String getUri() {
        return url;
    }
}
