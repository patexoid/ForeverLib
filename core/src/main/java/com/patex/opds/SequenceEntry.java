package com.patex.opds;

import com.patex.entities.Sequence;
import com.patex.utils.LinkUtils;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Created by Alexey on 23.05.2017.
 */
public class SequenceEntry implements OPDSEntryI {

    private final String id;
    private final String title;
    private final List<String> content;
    private final List<OPDSLink> links;

    public SequenceEntry(Sequence sequence) {

        id = "sequence:" + sequence.getId();
        title = sequence.getName();
        content = Collections.singletonList("Количество книг в серии: " + sequence.getBookSequences().size());
        links = Collections.singletonList(
                new OPDSLink(LinkUtils.makeURL("/opds/sequence", sequence.getId()), OPDSLink.OPDS_CATALOG));
    }


    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public Optional<List<String>> getContent() {
        return Optional.of(content);
    }

    @Override
    public List<OPDSLink> getLinks() {
        return links;
    }
}
