/* Generated by: JJTree: Do not edit this line. NumericParam.java Version 1.1 */
/* ParserGeneratorCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=false,TRACK_TOKENS=false,NODE_PREFIX=,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package com.patex.lrequest;

import lombok.Getter;

@Getter
public
class NumericParam extends DummyNode implements ValueSupplier {

  private int value;

  public void setValue(String value) {
    this.value = Integer.parseInt(value);
  }

  public NumericParam(int id) {
    super(id);
  }

  @Override
  public Value<Integer> getValueSupplier(ActionHandlerStorage handlerStorage) {
    return new Value<>(Integer.class, this::getValue);
  }
}
/* ParserGeneratorCC - OriginalChecksum=d4d0c825e7f346b60c9de8672dade3d2 (do not edit this line) */
