package com.lemoulinstudio.small.jse;

import com.lemoulinstudio.small.common.Remote;

/**
 *
 * @author Vincent Cantin
 * @param <T> The type of the viewed object.
 */
public interface ViewFactory<T extends Remote> {

  // This is required to select which factory to use for a given type.
  public Class<T> getViewedObjectClass();

  /**
   * Creates a view of the remote object represented by the provided proxy.
   *
   * @param proxy The proxy to be wrapped in a view.
   * @return A view of the remote object.
   */
  public View<T> createView(T proxy);

}
