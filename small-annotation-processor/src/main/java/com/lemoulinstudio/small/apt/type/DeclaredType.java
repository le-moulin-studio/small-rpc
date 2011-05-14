package com.lemoulinstudio.small.apt.type;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Vincent Cantin
 */
public class DeclaredType extends Type {

  private String typeName;
  private Type superType;
  private List<Type> implementedTypeList;
  private List<Type> genericArgumentTypeList;

  public DeclaredType(String typeName) {
    this(typeName, null, Collections.<Type>emptyList(), Collections.<Type>emptyList());
  }
  
  public DeclaredType(String typeName, Type superType, List<Type> implementedTypeList, List<Type> genericArgumentTypeList) {
    super(TypeKind.Declared);
    this.typeName = typeName;
    this.superType = superType;
    this.implementedTypeList = Collections.unmodifiableList(new ArrayList<Type>(implementedTypeList));
    this.genericArgumentTypeList = Collections.unmodifiableList(new ArrayList<Type>(genericArgumentTypeList));
  }

  public String getTypeName() {
    return typeName;
  }

  public Class<?> getTypeClass() {
    try {return Class.forName(typeName);}
    catch (ClassNotFoundException ex) {return null;}
  }

  public Type getSuperType() {
    return superType;
  }

  public List<Type> getImplementedTypeList() {
    return implementedTypeList;
  }

  public List<Type> getGenericArgumentTypeList() {
    return genericArgumentTypeList;
  }

  @Override
  public String toString() {
    String argString = "";
    for (int i = 0; i < genericArgumentTypeList.size(); i++) {
      if (i > 0) argString += ", ";
      argString += genericArgumentTypeList.get(i).toString();
    }

    String result = typeName;
    if (genericArgumentTypeList.size() > 0)
      result += "<" + argString + ">";

    return result;
  }

}
