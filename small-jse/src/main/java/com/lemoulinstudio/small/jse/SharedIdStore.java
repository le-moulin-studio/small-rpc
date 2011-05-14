package com.lemoulinstudio.small.jse;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 *
 * @author Vincent Cantin
 */
class SharedIdStore implements LocalIdStore {

  private Deque<Id> recycledIdStack;
  private int nextId;

  public SharedIdStore() {
    recycledIdStack = new ArrayDeque<Id>();
    nextId = 0;
  }

  @Override
  public Id createId() {
    if (recycledIdStack.isEmpty()) return new Id(nextId++);
    else return recycledIdStack.pop();
  }

  @Override
  public void releaseId(Id id) {
    assert id.getValue() < 0;
    recycledIdStack.push(id);
  }

}
