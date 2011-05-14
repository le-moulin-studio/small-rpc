package com.lemoulinstudio.small.apt.oom;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Vincent Cantin
 */
public class ModelMethod {

  ModelClass parentModelClass;
  String name;
  boolean logMethodInvocation;
  boolean logMessageReception;
  int methodId;
  List<ModelParameter> parameterList = new ArrayList<ModelParameter>();

  public ModelClass getParentModelClass() {
    return parentModelClass;
  }

  public String getName() {
    return name;
  }

  public boolean shouldLogMethodInvocation() {
    return logMethodInvocation;
  }

  public boolean shouldLogMessageReception() {
    return logMessageReception;
  }

  public int getMethodId() {
    return methodId;
  }

  public List<ModelParameter> getParameterList() {
    return parameterList;
  }

  
}
