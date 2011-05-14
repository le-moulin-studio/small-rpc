package com.lemoulinstudio.small.jse;

import com.lemoulinstudio.small.common.Remote;

/**
 * Interface for local objects who represent a view of a remote one,
 * which is typically a proxy.
 *
 * @author Vincent Cantin
 */
public interface View<T extends Remote> {

  /**
   * Returns the viewed object.
   *
   * @return The viewed object.
   */
  public T getViewedObject();
}
