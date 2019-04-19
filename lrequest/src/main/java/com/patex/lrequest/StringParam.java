/* Generated by: JJTree: Do not edit this line. StringParam.java Version 1.1 */
/* ParserGeneratorCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=false,TRACK_TOKENS=false,NODE_PREFIX=,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package com.patex.lrequest;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public
class StringParam extends DummyNode implements ValueSupplier {

  private String value;

  public StringParam(int id) {
    super(id);
  }

  @Override
  public RequestResult<String> getValueSupplier(ActionHandlerStorage handlerStorage) {
    return new RequestResult<>(String.class, this::getValue);
  }
}
/* ParserGeneratorCC - OriginalChecksum=fa9e40956f268a8a3ee6f411b16f1850 (do not edit this line) */
