package com.patex.lrequest;

import org.junit.Test;

public class ParserTest {



  @Test
  public void testParser() throws Exception{
    java.io.InputStream is=new java.io.ByteArrayInputStream("Author:page(6,soemthidf(sdsd):sdsw):alphabet(\"jhdsр аоы\")".getBytes());
    LibRequestBuilder t = new LibRequestBuilder(is, java.nio.charset.Charset.forName("UTF-8"));
    SimpleNode n = t.Request();
  }
}
