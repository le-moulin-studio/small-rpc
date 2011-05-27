package com.lemoulinstudio.small.apt.oom;

import com.lemoulinstudio.small.apt.type.Type;

/**
 *
 * @author Vincent Cantin
 */
public class ModelField {

  VoClass parentVoClass;
  Type type;
  String name;

  public VoClass getParentVoClass() {
    return parentVoClass;
  }

  public Type getType() {
    return type;
  }

  public String getName() {
    return name;
  }
  
}
