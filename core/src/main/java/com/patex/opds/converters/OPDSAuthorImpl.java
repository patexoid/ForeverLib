package com.patex.opds.converters;

import com.patex.entities.Author;

/**
 * Created by Alexey on 23.05.2017.
 */
public class OPDSAuthorImpl implements OPDSAuthor {

    private final String name;
    private final String uri;

    public OPDSAuthorImpl(Author author) {
        name = author.getName();
        uri = "/opds/author/" + author.getId();
    }

    public OPDSAuthorImpl(String name, String uri) {
        this.name = name;
        this.uri = uri;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getUri() {
        return uri;
    }
}
