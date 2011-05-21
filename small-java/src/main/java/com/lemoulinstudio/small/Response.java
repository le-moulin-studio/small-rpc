package com.lemoulinstudio.small;

/**
 * This is a container for the value returned by the invocation of the function
 * of  a remote service.
 *
 * @author Vincent Cantin
 */
public class Response<T> {
  
  protected T value;

  public synchronized void setValue(T value) {
    this.value = value;
    this.notify();
  }

  public synchronized T waitForValue() {
    try {wait();}
    finally {return value;}
  }
  
//  public T getValue() {
//    return value;
//  }
  
}
