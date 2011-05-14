package com.lemoulinstudio.small.jse;

import com.lemoulinstudio.small.common.Remote;

/**
 * The super type of all the proxy classes generated by the annotation processor.
 *
 * The generic type is only here to simplify the reading of the source code
 * and to help users avoid bugs in their code.
 *
 * @author Vincent Cantin
 */
public class Proxy<T extends Remote> {
  
  protected SmallSessionImpl smallSession;

  public Proxy(SmallSessionImpl smallSession) {
    this.smallSession = smallSession;
  }

}
