package com.lemoulinstudio.small.apt.type;

/**
 *
 * @author Vincent Cantin
 */
public class ArrayType extends Type {

  private Type componentType;

  public ArrayType(Type componentType) {
    super(TypeKind.Array);
    this.componentType = componentType;
  }

  public Type getComponentType() {
    return componentType;
  }

  public Type getTypeWithNoArray() {
    Type type = this;
    while (type.getTypeKind() == TypeKind.Array)
      type = ((ArrayType) type).getComponentType();

    return type;
  }

  public String bracketToString() {
    String result = "";
    for (Type type = this;
        type.getTypeKind() == TypeKind.Array;
        type = ((ArrayType) type).getComponentType())
      result += "[]";

    return result;
  }

  @Override
  public String toString() {
    return componentType.toString() + "[]";
  }

}
