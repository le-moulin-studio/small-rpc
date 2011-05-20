package com.lemoulinstudio.small.apt.oom;

import com.lemoulinstudio.small.apt.type.Type;
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
  int returnMethodId;
  List<ModelParameter> parameterList = new ArrayList<ModelParameter>();
  Type returnType;

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

  public int getReturnMethodId() {
    return returnMethodId;
  }

  public List<ModelParameter> getParameterList() {
    return parameterList;
  }

  public Type getReturnType() {
    return returnType;
  }

}
