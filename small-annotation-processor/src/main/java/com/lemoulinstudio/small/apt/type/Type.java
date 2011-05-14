package com.lemoulinstudio.small.apt.type;

/**
 *
 * @author Vincent Cantin
 */
public abstract class Type {

  private TypeKind typeKind;

  public Type(TypeKind typeKind) {
    this.typeKind = typeKind;
  }

  public TypeKind getTypeKind() {
    return typeKind;
  }

}
