package com.patex.messaging;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.StringTokenizer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class TextSpliterator {

    private final int maxMessageSize;
    private final List<String> delims;

    public TextSpliterator(int maxMessageSize, List<String> delims) {
        this.maxMessageSize = maxMessageSize;
        this.delims = new ArrayList<>(delims);
    }

    public List<String> splitText(String message) {
        List<String> chunks = createTextStream(message, delims.get(0)).collect(Collectors.toList());
        List<String> result = new ArrayList<>(Collections.singleton(""));
        for (String chunk : chunks) {
            int lastIndex = result.size() - 1;
            String lastElem = result.get(lastIndex);
            if (lastElem.length() + chunk.length() < maxMessageSize) {
                result.set(lastIndex, lastElem + chunk);
            } else {
                result.add(chunk);
            }
        }
        return result.stream().map(String::trim).collect(Collectors.toList());
    }

    private Stream<String> createTextStream(String message, String delim) {
        if (message.length() < maxMessageSize) {
            return Stream.of(message);
        } else {
            StringTokenizer st = new StringTokenizer(message, delim, true);
            if (st.countTokens() > 1) {
                return getStream(st).flatMap(s -> createTextStream(s, delim));
            } else {
                int index = delims.indexOf(delim);
                if (index < delims.size() - 1) {
                    return createTextStream(message, delims.get(index + 1));
                } else {
                    Stream<String> first =
                            createTextStream(message.substring(0, message.length() / 2), delim);
                    Stream<String> second =
                            createTextStream(message.substring(message.length() / 2), delim);
                    return Stream.concat(first, second);
                }
            }
        }
    }

    private Stream<String> getStream(StringTokenizer st) {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(new Iterator<String>() {
                    @Override
                    public boolean hasNext() {
                        return st.hasMoreTokens();
                    }

                    @Override
                    public String next() {
                        return st.nextToken();
                    }
                }, Spliterator.ORDERED),
                false);
    }

}
