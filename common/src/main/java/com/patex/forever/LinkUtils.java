package com.patex.forever;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * Created by Alexey on 07.05.2017.
 */
public class LinkUtils {
    private static final String SEPARATOR = "/";

    public static String makeURL(Object... parts) {
        return Arrays.stream(parts).map(String::valueOf).
                map(s -> s.startsWith(SEPARATOR) ? s.substring(1) : s).
                map(s -> s.endsWith(SEPARATOR) ? s.substring(0, s.length() - 1) : s)
                .reduce("", (s, s2) -> s + SEPARATOR + s2);
    }

    public static String encode(String text) {
        return URLEncoder.encode(text, StandardCharsets.UTF_8);
    }

    public static String decode(String text) {
        return URLDecoder.decode(text, StandardCharsets.UTF_8);
    }

}
