package com.lemoulinstudio.small.mina;

import com.lemoulinstudio.small.SmallSession;

/**
 *
 * @author Vincent Cantin
 */
public interface SmallRpcSessionListener {
  public void sessionCreated(SmallSession smallSession);
  public void sessionOpened(SmallSession smallSession);
  public void sessionClosed(SmallSession smallSession);
}
