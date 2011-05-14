package com.lemoulinstudio.small.jse;

/**
 *
 * @author Vincent Cantin
 */
class LocalIdStoreImpl implements LocalIdStore {

  private int nextIdValue;

  @Override
  public Id createId() {
    return new Id(nextIdValue++);
  }

  @Override
  public void releaseId(Id id) {
    // In this implementation, we just forget about the id and that's all.
    // There may be a id-value-loop bug on big user's projects, I am aware of that.
  }

}
