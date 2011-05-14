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

}
