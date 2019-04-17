package com.patex.lrequest;

public class DummyNode implements Node  {

  private final int id;

  public DummyNode(int id) {
    this.id = id;
  }

  @Override
  public void jjtOpen() {

  }

  @Override
  public void jjtClose() {

  }

  @Override
  public void jjtSetParent(Node n) {

  }

  @Override
  public Node jjtGetParent() {
    return null;
  }

  @Override
  public void jjtAddChild(Node n, int i) {

  }

  @Override
  public Node jjtGetChild(int i) {
    return null;
  }

  @Override
  public int jjtGetNumChildren() {
    return 0;
  }

  @Override
  public int getId() {
    return id;
  }
}
