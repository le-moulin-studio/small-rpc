package com.lemoulinstudio.small.common;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 *
 * @author Vincent Cantin
 */
public interface Encoder<T> {
  public void encode(T obj, DataOutput out) throws IOException;
  public T decode(DataInput in) throws IOException;
}
