package com.patex.forever.parser;

import java.io.Closeable;
import java.util.Iterator;

interface CloseableIterator extends Iterator<String>, Closeable {

}
