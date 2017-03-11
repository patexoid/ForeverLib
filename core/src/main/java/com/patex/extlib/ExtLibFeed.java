package com.patex.extlib;

import com.rometools.rome.feed.atom.Entry;
import com.rometools.rome.feed.atom.Link;

import java.util.List;

/**
 *
 */
public class ExtLibFeed {

    private final String title;
    private final List<Entry> entries;
    private final List<Link> links;


    public ExtLibFeed(String title, List<Entry> entries, List<Link> links) {
        this.title = title;
        this.entries = entries;
        this.links = links;
    }

    public String getTitle() {
        return title;
    }


    public List<Entry> getEntries() {
        return entries;
    }

    public List<Link> getLinks() {
        return links;
    }
}
