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
  
  public String getQualifiedName() {
    return type.getTypeName();
  }
  
  public String getPackageName() {
    String qualifiedName = getQualifiedName();
    return qualifiedName.substring(0, qualifiedName.lastIndexOf("."));
  }
  
  public String getSimpleName() {
    String qualifiedName = getQualifiedName();
    return qualifiedName.substring(qualifiedName.lastIndexOf(".") + 1);
  }
  
}
