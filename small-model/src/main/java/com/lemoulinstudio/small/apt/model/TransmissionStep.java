package com.lemoulinstudio.small.apt.model;

/**
 *
 * @author Vincent Cantin
 */
public enum TransmissionStep {
  Invocation,
  Reception,
  Both;

  public boolean hasInvocation() {
    return (this == Invocation) || (this == Both);
  }
  
  public boolean hasReception() {
    return (this == Reception) || (this == Both);
  }
}
