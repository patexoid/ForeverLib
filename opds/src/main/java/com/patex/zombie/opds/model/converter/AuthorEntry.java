package com.patex.zombie.opds.model.converter;

import com.patex.entities.AuthorEntity;
import com.patex.utils.LinkUtils;
import com.patex.utils.Res;
import com.patex.zombie.opds.model.OPDSContent;
import com.patex.zombie.opds.model.OPDSEntry;
import com.patex.zombie.opds.model.OPDSLink;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

/**
 * Created by Alexey on 07.05.2017.
 */
public class AuthorEntry implements OPDSEntry {

    private final String id;
    private final Res title;
    private final List<OPDSContent> content;
    private final List<OPDSLink> links;
    private final Instant date;


    public AuthorEntry(AuthorEntity author) {
        id = "author:" + author.getId();
        title = new Res("first.value", author.getName());
        if (author.getDescr() != null) {
            content = Collections.singletonList(new OPDSContent(author.getDescr()));
        } else {
            content = Collections.emptyList();
        }
        links = Collections.singletonList(
                new OPDSLink(LinkUtils.makeURL("opds", "author", author.getId()), OPDSLink.OPDS_CATALOG)
        );
        date = author.getUpdated();

    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Res getTitle() {
        return title;
    }

    @Override
    public List<OPDSContent> getContent() {
        return content;
    }

    @Override
    public List<OPDSLink> getLinks() {
        return links;
    }

    @Override
    public Instant getUpdated() {
        return date;
    }

    @Override
    public List<OPDSAuthor> getAuthors() {
        return Collections.emptyList();
    }
}
