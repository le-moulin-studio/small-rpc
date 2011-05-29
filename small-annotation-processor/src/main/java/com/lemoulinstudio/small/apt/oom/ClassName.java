package com.lemoulinstudio.small.apt.oom;

/**
 *
 * @author Vincent Cantin
 */
public class ClassName {

  private String className;

  public ClassName(String className) {
    this.className = className;
  }

  public String getQualifiedName() {
    return className;
  }

  public String getPackageName() {
    return className.substring(0, className.lastIndexOf("."));
  }

  public String getSimpleName() {
    return className.substring(className.lastIndexOf(".") + 1);
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof ClassName)) return false;
    else return className.equals(((ClassName) obj).className);
  }

  @Override
  public int hashCode() {
    return className.hashCode();
  }

  @Override
  public String toString() {
    return className;
  }

}
