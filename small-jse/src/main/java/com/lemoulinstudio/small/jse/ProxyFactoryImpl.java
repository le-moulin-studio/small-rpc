package com.lemoulinstudio.small.jse;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.lemoulinstudio.small.common.RemoteService;

/**
 *
 * @author Vincent Cantin
 */
class ProxyFactoryImpl implements ProxyFactory {

  private Map<Class<? extends RemoteService>, Class> remoteClassToProxyClass;

  public ProxyFactoryImpl(Map<Class<? extends RemoteService>, Class> remoteClassToProxyClass) {
    this.remoteClassToProxyClass = remoteClassToProxyClass;
  }

  @Override
  public <T extends RemoteService> T createProxy(SmallSessionImpl smallSession, Class<T> remoteClass) {
    try {
      Class<? extends RemoteService> proxyClass = remoteClassToProxyClass.get(remoteClass);
      RemoteService proxy = proxyClass.getConstructor(SmallSessionImpl.class).newInstance(smallSession);
      return (T) proxy;
    } catch (Exception ex) {
      Logger.getLogger(ProxyFactoryImpl.class.getName()).log(Level.SEVERE, null, ex);
      return null;
    }
  }
  
}
