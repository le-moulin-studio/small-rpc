package com.lemoulinstudio.small.jse;

import com.lemoulinstudio.small.common.Remote;

/**
 *
 * @author Vincent Cantin
 */
interface ProxyFactory {
  public <T extends Remote> T createProxy(SmallSessionImpl smallSession, Class<T> clazz);
}
