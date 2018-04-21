package com.patex.shingle.byteSet;

import java.util.Iterator;
import java.util.function.BiFunction;
import java.util.function.Function;


public class ByteHashSet implements Iterable<byte[]> {

    private static final int MAXIMUM_CAPACITY = 1 << 30;

    private transient Node[] table;
    private int size = 0;
    private final Function<byte[], Node> createNode;
    private final BiFunction<byte[], Node, Node> createNextNode;
    private final int byteArraySize;

    ByteHashSet(int initialCapacity,
                int byteArraySize,
                Function<byte[], Node> createNode,
                BiFunction<byte[], Node, Node> createNextNode) {
        if (initialCapacity < 0)
            throw new IllegalArgumentException("Illegal initial capacity: " +
                    initialCapacity);
        if (initialCapacity > MAXIMUM_CAPACITY)
            initialCapacity = MAXIMUM_CAPACITY;
        this.byteArraySize = byteArraySize;
        table = new Node[tableSizeFor(initialCapacity)];
        this.createNode = createNode;
        this.createNextNode = createNextNode;
    }

    private static int tableSizeFor(int cap) {
        int n = cap - 1;
        n |= n >>> 1;
        n |= n >>> 2;
        n |= n >>> 4;
        n |= n >>> 8;
        n |= n >>> 16;
        return n < 0 ? 1 : (n >= MAXIMUM_CAPACITY) ? MAXIMUM_CAPACITY : n + 1;
    }

    static int getHashCode(byte[] key, int byteArraySize) {
        int result = 1;
        for (int i = 0; i < byteArraySize; i++) {
            result = 31 * result + key[i];
        }
        return result;
    }

    private int index(int hashCode) {
        int i = hashCode ^ (hashCode >>> 16);
        return (table.length - 1) & i;
    }

    public boolean contains(byte[] key) {
        int hashCode = getHashCode(key, byteArraySize);
        int index = index(hashCode);
        Node node = table[index];
        if (node != null) {
            do {
                if (node.hashCode() == hashCode && node.isEqualsArray(key))
                    return true;
            } while ((node = node.getNext()) != null);
        }
        return false;
    }

    public void add(byte[] key) {
        int hashCode = getHashCode(key, byteArraySize);
        int index = index(hashCode);
        Node node = table[index];
        if (node == null) {
            table[index] = createNode.apply(key);
            size++;
        } else {
            do {
                if (node.hashCode() == hashCode && node.isEqualsArray(key))
                    return;
            } while ((node = node.getNext()) != null);
            table[index] = createNextNode.apply(key, table[index]);
            size++;
        }
    }

    public int getSize() {
        return size;
    }

    public int getByteArraySize() {
        return byteArraySize;
    }

    @Override
    public Iterator<byte[]> iterator() {
        return new ByteIterator();
    }

    private class ByteIterator implements Iterator<byte[]> {
        int position = 0;
        Node node;

        ByteIterator() {
            nextBucket();
        }

        private void nextBucket() {
            for (int i = position; i < table.length; i++) {
                if (table[i] != null) {
                    node = table[i];
                    position = i + 1;
                    return;
                }
            }
            node = null;
        }

        @Override
        public boolean hasNext() {
            return node != null;
        }

        @Override
        public byte[] next() {
            byte[] bytes = node.toBytes();
            node = node.getNext();
            if (node == null) {
                nextBucket();
            }
            return bytes;
        }
    }

}
