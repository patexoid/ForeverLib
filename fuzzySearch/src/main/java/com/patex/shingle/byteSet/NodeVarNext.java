package com.patex.shingle.byteSet;

class NodeVarNext extends NodeVar {
    private final Node next;

    NodeVarNext(byte[] key, Node next) {
        super(key);
        this.next = next;
    }

    @Override
    public Node getNext() {
        return next;
    }
}
