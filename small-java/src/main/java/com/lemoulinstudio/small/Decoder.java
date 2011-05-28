package com.lemoulinstudio.small;

import java.io.DataInputStream;
import java.io.IOException;

/**
 *
 * @author Vincent Cantin
 */
public interface Decoder {
  public void decodeAndInvoke(SmallSessionImpl smallSession, SmallDataInputStream inputStream) throws IOException;
}
