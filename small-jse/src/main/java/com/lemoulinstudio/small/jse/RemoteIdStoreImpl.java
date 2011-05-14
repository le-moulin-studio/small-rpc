package com.lemoulinstudio.small.jse;

/**
 *
 * @author Vincent Cantin
 */
class RemoteIdStoreImpl implements RemoteIdStore {

  private int nextIdValue;

  @Override
  public Id allocateNewId() {
    return new Id(nextIdValue++);
  }

  @Override
  public void allocateId(Id id) {
    // In this implementation, we just forget about the id and that's all.
  }

  @Override
  public void releaseId(Id id) {
    // In this implementation, we just forget about the id and that's all.
  }

}
