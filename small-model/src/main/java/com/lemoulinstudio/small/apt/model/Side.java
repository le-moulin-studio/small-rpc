package com.lemoulinstudio.small.apt.model;

/**
 *
 * @author Vincent Cantin
 */
public enum Side {
  Invocation,
  Reception,
  Both;

  public boolean isInvocationSide() {
    return (this == Invocation) || (this == Both);
  }
  
  public boolean isReceptionSide() {
    return (this == Reception) || (this == Both);
  }
}
