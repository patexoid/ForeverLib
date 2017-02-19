package com.patex.extlib;

import com.rometools.rome.feed.atom.Entry;

import java.util.List;

/**
 *
 */
public class ExtLibFeed {

    private final String title;
    private final List<Entry> entries;
    private final String link;

    public ExtLibFeed(String title, String link, List<Entry> entries) {
        this.title = title;
        this.link = link;
        this.entries = entries;
    }

    public String getTitle() {
        return title;
    }

    public String getLink() {
        return link;
    }

    public List<Entry> getEntries() {
        return entries;
    }
}
