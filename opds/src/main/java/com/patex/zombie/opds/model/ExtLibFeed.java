package com.patex.zombie.opds.model;

import com.patex.zombie.opds.model.converter.ExtLibOPDSEntry;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 */
public class ExtLibFeed {

    private final String title;
    private final List<OPDSEntry> entries;
    private final List<OPDSLink> links;


    public ExtLibFeed(String title, List<OPDSEntry> entries, List<OPDSLink> links) {
        this.title = title;
        this.entries = entries;
        this.links = links;
    }

    public String getTitle() {
        return title;
    }


    public List<OPDSEntry> getEntries() {
        return entries;
    }

    public List<OPDSLink> getLinks() {
        return links;
    }


    public ExtLibFeed updateWithPrefix(String prefix){
        List<OPDSEntry> entries = this.entries.stream().
                map(entry -> new ExtLibOPDSEntry(entry, prefix)).collect(Collectors.toList());
        List<OPDSLink> links = updateLinks(this.links, prefix)
                .collect(Collectors.toList());
        return new ExtLibFeed(title, entries,links);
    }

    private Stream<OPDSLink> updateLinks(List<OPDSLink> links, String prefix) {
        return links.stream().
                map(link -> new OPDSLink(prefix + link.getHref(), link.getRel(), link.getType()));
    }

}
