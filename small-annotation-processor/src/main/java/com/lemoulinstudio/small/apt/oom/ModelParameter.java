package com.lemoulinstudio.small.apt.oom;

import com.lemoulinstudio.small.apt.type.Type;

/**
 *
 * @author Vincent Cantin
 */
public class ModelParameter {

  ModelMethod parentModelMethod;
  Type type;
  String name;
  boolean isCallerObject;

  public ModelMethod getParentModelMethod() {
    return parentModelMethod;
  }

  public Type getType() {
    return type;
  }

  public String getName() {
    return name;
  }

  public boolean isCallerObject() {
    return isCallerObject;
  }

}
