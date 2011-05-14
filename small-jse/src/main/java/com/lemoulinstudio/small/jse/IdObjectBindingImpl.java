package com.lemoulinstudio.small.jse;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

/**
 *
 * @author Vincent Cantin
 */
class IdObjectBindingImpl<T> implements IdObjectBinding<T> {

  private Map<Id, T> idToObject;
  private Map<T, Id> objectToId;

  public IdObjectBindingImpl() {
    idToObject = new HashMap<Id, T>();
    objectToId = new IdentityHashMap<T, Id>();
  }
  
  @Override
  public void bind(Id id, T object) {
    assert id != null : "Id should not be null.";
    assert !idToObject.containsKey(id) : "The id is already used.";
    assert !objectToId.containsKey(object) : "The object is already bound.";
    idToObject.put(id, object);
    objectToId.put(object, id);
  }
  
  @Override
  public void unbindFromId(Id id) {
    assert idToObject.containsKey(id) : "Nothing is bound to this id.";
    T object = idToObject.remove(id);
    objectToId.remove(object);
  }

  @Override
  public Id unbindFromObject(T object) {
    assert objectToId.containsKey(object) : "This object is not bound.";
    Id id = objectToId.remove(object);
    idToObject.remove(id);
    return id;
  }

  @Override
  public boolean isUsing(Id id) {
    return idToObject.containsKey(id);
  }

  @Override
  public T getObject(Id id) {
    return idToObject.get(id);
  }

  @Override
  public Id getId(T object) {
    return objectToId.get(object);
  }

}
