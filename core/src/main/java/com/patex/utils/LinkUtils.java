package com.patex.utils;

import java.util.Arrays;

/**
 * Created by Alexey on 07.05.2017.
 */
public class LinkUtils {

    public static final String SLASH = "/";

    public static String makeURL(Object... parts) {
        return Arrays.stream(parts).map(String::valueOf).
                map(s -> s.startsWith(SLASH) ? s.substring(1) : s).
                map(s -> s.endsWith(SLASH) ? s.substring(0,s.length()-1) : s)
                .reduce("", (s, s2) -> s + SLASH + s2);
    }
}
