package com.lemoulinstudio.small.apt.oom;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Vincent Cantin
 */
public class ModelData {
  
  int sameSideMethodId;
  int otherSideMethodId;

  Set<ModelClass> modelClassSet = new HashSet<ModelClass>();
  List<ModelClass> sameSideModelClassOrderedList = new ArrayList<ModelClass>();
  List<ModelClass> otherSideModelClassOrderedList = new ArrayList<ModelClass>();

  public int getNumberOfMethodsOnSameSide() {
    return sameSideMethodId;
  }

  public int getNumberOfMethodsOnOtherSide() {
    return otherSideMethodId;
  }

  public Set<ModelClass> getModelClassSet() {
    return modelClassSet;
  }

  public List<ModelClass> getSameSideModelClassOrderedList() {
    return sameSideModelClassOrderedList;
  }

  public List<ModelClass> getOtherSideModelClassOrderedList() {
    return otherSideModelClassOrderedList;
  }

}
