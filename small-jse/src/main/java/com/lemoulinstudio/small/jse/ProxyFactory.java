package com.lemoulinstudio.small.jse;

import com.lemoulinstudio.small.common.RemoteService;

/**
 *
 * @author Vincent Cantin
 */
interface ProxyFactory {
  public <T extends RemoteService> T createProxy(SmallSessionImpl smallSession, Class<T> clazz);
}
