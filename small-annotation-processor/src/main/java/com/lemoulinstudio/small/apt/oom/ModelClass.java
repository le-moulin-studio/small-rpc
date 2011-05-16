package com.lemoulinstudio.small.apt.oom;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Vincent Cantin
 */
public class ModelClass {

  boolean isLocalSide;
  String qualifiedName;
  List<ModelMethod> methodList = new ArrayList<ModelMethod>();

  public boolean isLocalSide() {
    return isLocalSide;
  }

  public String getQualifiedName() {
    return qualifiedName;
  }

  public String getPackageName() {
    return qualifiedName.substring(0, qualifiedName.lastIndexOf("."));
  }

  public String getSimpleName() {
    return qualifiedName.substring(qualifiedName.lastIndexOf(".") + 1);
  }

  public List<ModelMethod> getMethodList() {
    return methodList;
  }

}
