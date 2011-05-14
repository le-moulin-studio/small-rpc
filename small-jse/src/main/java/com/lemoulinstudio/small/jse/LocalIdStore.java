package com.lemoulinstudio.small.jse;

/**
 *
 * @author Vincent Cantin
 */
interface LocalIdStore {
  public Id createId();
  public void releaseId(Id id);
}
