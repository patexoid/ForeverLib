package com.patex.shingle.byteSet;

class Node8Next extends Node8 {
    private final Node next;

    Node8Next(byte[] key, Node next) {
        super(key);
        this.next = next;
    }

    @Override
    public Node getNext() {
        return next;
    }
}
