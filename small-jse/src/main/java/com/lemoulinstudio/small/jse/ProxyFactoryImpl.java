package com.lemoulinstudio.small.jse;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.lemoulinstudio.small.common.Remote;

/**
 *
 * @author Vincent Cantin
 */
class ProxyFactoryImpl implements ProxyFactory {

  private Map<Class<? extends Remote>, Class> remoteClassToProxyClass;

  public ProxyFactoryImpl(Map<Class<? extends Remote>, Class> remoteClassToProxyClass) {
    this.remoteClassToProxyClass = remoteClassToProxyClass;
  }

  @Override
  public <T extends Remote> T createProxy(SmallSessionImpl smallSession, Class<T> remoteClass) {
    try {
      Class<? extends Remote> proxyClass = remoteClassToProxyClass.get(remoteClass);
      Remote proxy = proxyClass.getConstructor(SmallSessionImpl.class).newInstance(smallSession);
      return (T) proxy;
    } catch (Exception ex) {
      Logger.getLogger(ProxyFactoryImpl.class.getName()).log(Level.SEVERE, null, ex);
      return null;
    }
  }
  
}
