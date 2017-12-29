package com.patex.shingle.byteSet;

import java.util.Iterator;
import java.util.function.BiFunction;
import java.util.function.Function;


public class ByteHashSet implements Iterable<byte[]> {

    private static final int MAXIMUM_CAPACITY = 1 << 30;

    private transient Node[] table;
    private int size=0;
    private Function<byte[], Node> createNode;
    private BiFunction<byte[], Node, Node> createNextNode;

    ByteHashSet(int initialCapacity,
                Function<byte[], Node> createNode,
                BiFunction<byte[], Node,Node> createNextNode) {
        if (initialCapacity < 0)
            throw new IllegalArgumentException("Illegal initial capacity: " +
                    initialCapacity);
        if (initialCapacity > MAXIMUM_CAPACITY)
            initialCapacity = MAXIMUM_CAPACITY;
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

    static int getHashCode(byte[] key) {
        if (key == null)
            return 0;

        int result = 1;
        for (byte element : key)
            result = 31 * result + element;

        return result;
    }

    private int index(int hashCode) {
        int i = hashCode ^ (hashCode >>> 16);
        return (table.length - 1) & i;
    }

    public boolean contains(byte[] key) {
        int hashCode = getHashCode(key);
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
        int hashCode = getHashCode(key);
        int index = index(hashCode);
        Node node = table[index];
        if (node == null) {
            table[index] = createNode.apply(key );
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

    @Override
    public Iterator<byte[]> iterator() {
        return new ByteIterator();
    }

    private class ByteIterator implements Iterator<byte[]> {
        int position = 0;
        Node node;

        public ByteIterator() {
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
            node=null;
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
