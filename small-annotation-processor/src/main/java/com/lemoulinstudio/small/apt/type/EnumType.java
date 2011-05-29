package com.lemoulinstudio.small.apt.type;

import com.lemoulinstudio.small.apt.oom.ClassName;
import java.util.List;

/**
 * This represents an enum type.
 * @author Vincent Cantin
 */
public class EnumType extends Type {

  private ClassName className;
  private List<String> enumItems;

  public EnumType(String qualifiedName, List<String> enumItems) {
    super(TypeKind.Enum);
    this.className = new ClassName(qualifiedName);
    this.enumItems = enumItems;
  }

  public ClassName getClassName() {
    return className;
  }

  public List<String> getEnumItems() {
    return enumItems;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof EnumType)) return false;
    else return className.equals(((EnumType) obj).className);
  }

  @Override
  public int hashCode() {
    return className.hashCode();
  }

  @Override
  public String toString() {
    return className.getQualifiedName();
  }

}
