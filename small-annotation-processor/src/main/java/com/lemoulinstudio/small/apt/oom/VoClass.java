package com.lemoulinstudio.small.apt.oom;

import com.lemoulinstudio.small.apt.type.DeclaredType;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Vincent Cantin
 */
public class VoClass {
  
  DeclaredType type;
  List<ModelField> fieldList = new ArrayList<ModelField>();

  public DeclaredType getType() {
    return type;
  }

  public List<ModelField> getFieldList() {
    return fieldList;
  }
  
}
