package com.patex.lrequest;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
public class FlowType {

  public static FlowType INITIAL = new FlowType(null, Type.initial);
  private final Class returnType;
  private final Type type;


  private FlowType(Class returnType, Type type) {
    this.returnType = returnType;
    this.type = type;
  }

  public static FlowType streamResult(Class returnType) {
    return new FlowType(returnType, Type.stream);
  }


  public static FlowType objResult(Class returnType) {
    return new FlowType(returnType, Type.object);
  }

  public boolean is(Type type) {
    return this.type.equals(type);
  }

  public enum Type {
    initial, stream, object
  }
}
