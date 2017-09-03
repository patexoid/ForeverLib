package com.patex.parser;

import java.io.Closeable;
import java.util.Iterator;

interface CloseableIterator extends Iterator<String>, Closeable {

}
