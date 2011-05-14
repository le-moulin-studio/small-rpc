package com.lemoulinstudio.small.apt.type;

import java.util.HashMap;
import java.util.Map;

/**
 * This includes primitive wrappers.
 * @author Vincent Cantin
 */
public class PrimitiveWrapperType extends Type {

  private static Map<Class, Class> wrapperClassToPrimitiveClass;

  private static Map<Class, Class> primitiveClassToWrapperClass;

  static {
    wrapperClassToPrimitiveClass = new HashMap<Class, Class>();
    wrapperClassToPrimitiveClass.put(Boolean.class,   boolean.class);
    wrapperClassToPrimitiveClass.put(Character.class, char.class);
    wrapperClassToPrimitiveClass.put(Byte.class,      byte.class);
    wrapperClassToPrimitiveClass.put(Short.class,     short.class);
    wrapperClassToPrimitiveClass.put(Integer.class,   int.class);
    wrapperClassToPrimitiveClass.put(Long.class,      long.class);
    wrapperClassToPrimitiveClass.put(Float.class,     float.class);
    wrapperClassToPrimitiveClass.put(Double.class,    double.class);

    primitiveClassToWrapperClass = new HashMap<Class, Class>();
    primitiveClassToWrapperClass.put(boolean.class,   Boolean.class);
    primitiveClassToWrapperClass.put(char.class,      Character.class);
    primitiveClassToWrapperClass.put(byte.class,      Byte.class);
    primitiveClassToWrapperClass.put(short.class,     Short.class);
    primitiveClassToWrapperClass.put(int.class,       Integer.class);
    primitiveClassToWrapperClass.put(long.class,      Long.class);
    primitiveClassToWrapperClass.put(float.class,     Float.class);
    primitiveClassToWrapperClass.put(double.class,    Double.class);
  }

  public static Class getPrimitiveClass(Class wrapperClass) {
    return wrapperClassToPrimitiveClass.get(wrapperClass);
  }

  public static Class getWrapperClass(Class primitiveClass) {
    return primitiveClassToWrapperClass.get(primitiveClass);
  }

  public static boolean isWrapperClass(String className) {
    for (Class wrapperClass : wrapperClassToPrimitiveClass.keySet())
      if (wrapperClass.getName().equals(className))
        return true;

    return false;
  }

  private Class wrapperClass;
  
  public PrimitiveWrapperType(Class wrapperClass) {
    super(TypeKind.PrimitiveWrapper);
    this.wrapperClass = wrapperClass;
  }

  public PrimitiveWrapperType(String wrapperClassName) {
    super(TypeKind.PrimitiveWrapper);
    try {this.wrapperClass = Class.forName(wrapperClassName);}
    catch (ClassNotFoundException ex) {throw new Error(ex);}
  }

  public Class getWrapperClass() {
    return wrapperClass;
  }

  public Class getPrimitiveClass() {
    return getPrimitiveClass(getWrapperClass());
  }

  @Override
  public String toString() {
    return getWrapperClass().getCanonicalName();
  }

}
