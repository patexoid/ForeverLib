package com.patex.shingle.byteSet;

class Node16Next extends Node16 {
    private final Node next;

    Node16Next(byte[] key, Node next) {
        super(key);
        this.next = next;
    }

    @Override
    public Node getNext() {
        return next;
    }
}
