package com.lemoulinstudio.small.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * Utility class which provides a mean to read data from a {@link java.nio.ByteBuffer}
 * using an {@link java.io.InputStream}.
 *
 * @author Vincent Cantin
 */
public class ByteBufferInputStream extends InputStream {

  public ByteBuffer byteBuffer;

  public ByteBufferInputStream(ByteBuffer byteBuffer) {
    this.byteBuffer = byteBuffer;
  }

  @Override
  public synchronized int read() throws IOException {
    if (byteBuffer.hasRemaining()) return byteBuffer.get() & 0xff;
    else return -1;
  }

  @Override
  public int read(byte[] b, int off, int len) throws IOException {
    len = Math.min(len, byteBuffer.remaining());
    byteBuffer.get(b, off, len);
    return len;
  }

}
