package com.lemoulinstudio.small.jse;

/**
 * @author Vincent Cantin
 */
interface IdObjectBinding<T> {
  public void bind(Id id, T object);
  public void unbindFromId(Id id);
  public Id unbindFromObject(T object);
  public boolean isUsing(Id id);
  public T getObject(Id id);
  public Id getId(T object);
}
