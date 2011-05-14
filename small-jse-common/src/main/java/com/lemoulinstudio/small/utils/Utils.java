package com.lemoulinstudio.small.utils;

import java.lang.reflect.Array;
import java.lang.reflect.Proxy;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Some utils used by Small.
 *
 * @author Vincent Cantin
 */
public class Utils {

  private static StringBuffer objectToStringBuffer(Object object) {
    if (object == null)
      return new StringBuffer("null");
    else if (object.getClass().isArray())
      return arrayToStringBuffer(object);
    else if (object instanceof String)
      return new StringBuffer("\"").append(object).append("\"");
    else if (object instanceof Enum)
      return new StringBuffer(object.getClass().getSimpleName()).append(".").append(object);
    else if (object instanceof Proxy)
      return new StringBuffer("proxy@").append(Integer.toHexString(System.identityHashCode(object)));
    else if (object instanceof List)
      return arrayToStringBuffer(((List) object).toArray());
    else if (object instanceof Set)
      return arrayToStringBuffer(((Set) object).toArray());
    else if (object instanceof Map)
      return arrayToStringBuffer(((Map) object).entrySet().toArray());
    else if (object instanceof Map.Entry) {
      Map.Entry entry = (Map.Entry) object;
      return new StringBuffer("[").
              append(objectToStringBuffer(entry.getKey())).append(", ").
              append(objectToStringBuffer(entry.getValue())).append("]");
    }
    else
      return new StringBuffer(object.toString());
  }

  private static StringBuffer arrayToStringBuffer(Object array) {
    StringBuffer buffer = new StringBuffer("[");

    int arrayLenght = Array.getLength(array);
    for (int i = 0; i < arrayLenght; i++) {
      buffer.append(objectToStringBuffer(Array.get(array, i)));
      if (i < arrayLenght - 1)
        buffer.append(", ");
    }

    buffer.append("]");

    return buffer;
  }

  public static String byteBufferToString(ByteBuffer buffer) {
    StringBuffer sb = new StringBuffer("[");

    if (buffer.hasRemaining()) sb.append(buffer.get());
    while (buffer.hasRemaining()) sb.append(", ").append(buffer.get());
    sb.append("]");

    buffer.rewind();

    return new String(sb);
  }


  public static String refToString(Object object) {
    if (object == null) return "null";
    else return object.getClass().getSimpleName() + "@" +
            Integer.toHexString(System.identityHashCode(object));
  }

  /**
   * Sends the current thread to sleep forever.
   */
  public static void waitForever() {
    Object obj = new Object();
    synchronized (obj) {
      while (true) {
        try {obj.wait();}
        catch (InterruptedException ex) {}
      }
    }
  }
  
}
