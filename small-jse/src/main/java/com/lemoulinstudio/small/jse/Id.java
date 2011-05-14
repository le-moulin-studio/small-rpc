package com.lemoulinstudio.small.jse;

import java.io.Serializable;

/**
 * A class which represents the Id of an object in the Small system.
 *
 * @author Vincent Cantin
 */
class Id implements Serializable {

  private static final long serialVersionUID = 1L;

  private int value;

  /**
   * Builds an Id instance.
   * 
   * @param value The id number for this Id instance.
   */
  public Id(int value) {
    this.value = value;
  }

  public int getValue() {
    return value;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof Id) return (this.value == ((Id) obj).value);
    else return false;
  }

  @Override
  public int hashCode() {
    return value;
  }

  @Override
  public String toString() {
    return "[value=" + value + "]";
  }

}
