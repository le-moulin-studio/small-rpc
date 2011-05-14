package com.lemoulinstudio.small.apt.type;

/**
 * This includes enums.
 * @author Vincent Cantin
 */
public class EnumType extends Type {

  private String qualifiedClassName;
  private int nbEnumItems;

  public EnumType(String qualifiedClassName, int nbEnumItems) {
    super(TypeKind.Enum);
    this.qualifiedClassName = qualifiedClassName;
    this.nbEnumItems = nbEnumItems;
  }

  public String getQualifiedClassName() {
    return qualifiedClassName;
  }

  public int getNbEnumItems() {
    return nbEnumItems;
  }

  @Override
  public String toString() {
    return getQualifiedClassName();
  }

}
