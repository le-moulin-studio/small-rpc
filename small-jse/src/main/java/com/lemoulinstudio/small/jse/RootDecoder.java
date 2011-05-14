package com.lemoulinstudio.small.jse;

import java.io.DataInputStream;
import java.io.IOException;

/**
 *
 * @author Vincent Cantin
 */
public interface RootDecoder {
  public void decodeAndInvoke(SmallSessionImpl smallSession, DataInputStream inputStream) throws IOException;
}
