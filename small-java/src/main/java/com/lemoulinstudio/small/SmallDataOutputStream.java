package com.lemoulinstudio.small;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 *
 * @author Vincent Cantin
 */
public class SmallDataOutputStream extends DataOutputStream {

  public SmallDataOutputStream(OutputStream out) {
    super(out);
  }
  
  public void writeMyUTF(String str) throws IOException {
    // This is the support for the null value.
    if (str == null) writeShort(65535);
    else writeUTF(str);
  }
  
}
