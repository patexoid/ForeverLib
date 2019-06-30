package com.patex.lrequest;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
public class DataType {

  public static DataType INITIAL = new DataType(null, Type.initial);
  private final Class returnType;
  private final Type type;


  private DataType(Class returnType, Type type) {
    this.returnType = returnType;
    this.type = type;
  }

  public static DataType streamResult(Class returnType) {
    return new DataType(returnType, Type.stream);
  }

  public static DataType objResult(Class returnType) {
    return new DataType(returnType, Type.object);
  }

  public boolean is(Type type) {
    return this.type.equals(type);
  }

  public enum Type {
    initial, stream, object
  }
}
