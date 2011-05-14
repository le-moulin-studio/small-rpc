package com.lemoulinstudio.small.jse;

/**
 *
 * @author Vincent Cantin
 */
interface RemoteIdStore {
  public Id allocateNewId();
  public void allocateId(Id id);
  public void releaseId(Id id);
}
