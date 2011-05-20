package com.lemoulinstudio.small.apt.type;

/**
 *
 * @author Vincent Cantin
 */
public class VoidType extends Type {

  private VoidType() {
    super(TypeKind.Void);
  }
  
  public static final VoidType instance = new VoidType();
}
