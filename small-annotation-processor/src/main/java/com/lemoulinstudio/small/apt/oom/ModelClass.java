package com.lemoulinstudio.small.apt.oom;

import com.lemoulinstudio.small.apt.model.HostKind;
import com.lemoulinstudio.small.apt.model.ImplementedBy;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Vincent Cantin
 */
public class ModelClass {

  HostKind hostKind;
  boolean isLocalSide;
  IdBindingPolicy idBindingPolicy;
  ImplementedBy implementedBy;
  String qualifiedName;
  ModelClass viewedModel;
  ModelClass viewedByModel;
  List<ModelMethod> methodList = new ArrayList<ModelMethod>();

  public HostKind getHostKind() {
    return hostKind;
  }

  public boolean isLocalSide() {
    return isLocalSide;
  }

  public IdBindingPolicy getIdBindingPolicy() {
    return idBindingPolicy;
  }

  public boolean isSingleton() {
    return idBindingPolicy == IdBindingPolicy.Singleton;
  }

  public boolean isImplementationSpecified() {
    return implementedBy != null;
  }

  public String getImplementationQualifiedName() {
    return implementedBy.value();
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

  public boolean isView() {
    return viewedModel != null;
  }

  public ModelClass getViewedModel() {
    return viewedModel;
  }

  public boolean isViewed() {
    return viewedByModel != null;
  }

  public ModelClass getViewedByModel() {
    return viewedByModel;
  }

  public List<ModelMethod> getMethodList() {
    return methodList;
  }

}
