package com.patex.zombie.core.parser;

import java.io.Closeable;
import java.util.Iterator;

interface CloseableIterator extends Iterator<String>, Closeable {

}
