package com.patex.zombie.opds.service;

import com.patex.model.AggrResult;
import com.patex.model.Author;
import com.patex.model.Sequence;
import com.patex.zombie.opds.api.AuthorClient;
import com.patex.zombie.opds.api.SequenceClient;
import com.patex.opds.OPDSEntry;
import com.patex.zombie.opds.model.converters.AuthorEntry;
import com.patex.utils.LinkUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
public class OpdsService {

    public static final String PREFIX = "opds";
    private static final int EXPAND_FOR_AUTHORS_COUNT = 3;
    private static final String AUTHORSINDEX = "authorsindex";
    private final AuthorClient authorClient;

    private final SequenceClient sequenceClient;


    public List<OPDSEntry> getAuthorsCount(String prefix) {
        return authorClient.getCount(prefix).stream().flatMap(this::expandAggrResult).collect(Collectors.toList());
    }

    private Stream<OPDSEntry> expandAggrResult(AggrResult aggr) {
        if (aggr.getCount() >= EXPAND_FOR_AUTHORS_COUNT) {
            return Stream.of(OPDSEntry.builder(aggr.getId(), "first.value", aggr.getId()).
                    addLink(LinkUtils.makeURL(PREFIX, AUTHORSINDEX, LinkUtils.encode(aggr.getId())))
                    .build());
        } else {
            return authorClient.getAuthors(aggr.getId()).stream().map(AuthorEntry::new);
        }
    }

    public Author getAuthor(long id) {
        return authorClient.getAuthor(id);
    }

    public Sequence getSequence(long id) {
        return sequenceClient.getSequence(id);
    }
}
