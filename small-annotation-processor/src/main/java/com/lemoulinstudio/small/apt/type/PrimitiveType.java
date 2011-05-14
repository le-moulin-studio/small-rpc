package com.lemoulinstudio.small.apt.type;

/**
 * This includes primitives.
 * @author Vincent Cantin
 */
public class PrimitiveType extends Type {

  private Class primitiveClass;

  public PrimitiveType(Class primitiveClass) {
    super(TypeKind.Primitive);
    this.primitiveClass = primitiveClass;
  }

  public Class getPrimitiveClass() {
    return primitiveClass;
  }

  @Override
  public String toString() {
    return getPrimitiveClass().getSimpleName();
  }

}
