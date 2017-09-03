package com.patex.utils.shingle;

import java.io.Closeable;
import java.util.Iterator;

/**
 *
 */
public interface Shingleable extends Iterator<String>, Closeable {

    int size();
}
