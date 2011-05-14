package com.lemoulinstudio.small.apt.type;

import com.lemoulinstudio.small.apt.oom.ModelClass;

/**
 *
 * @author Vincent Cantin
 */
public class ModelType extends Type {

  private ModelClass modelClass;

  public ModelType(ModelClass modelClass) {
    super(TypeKind.Model);
    this.modelClass = modelClass;
  }

  public ModelClass getModelClass() {
    return modelClass;
  }

  @Override
  public String toString() {
    return modelClass.getQualifiedName();
  }

}
