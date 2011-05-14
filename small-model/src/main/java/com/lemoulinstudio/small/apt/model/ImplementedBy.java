package com.lemoulinstudio.small.apt.model;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to specify the subclass of the annotated network interface model,
 * in the case there is only 1.
 *
 * The interfaces generated on the same side than the annotated network interface model
 * are using the specified subtype for the method parameters instead.
 *
 * @author Vincent Cantin
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.TYPE})
public @interface ImplementedBy {
  /**
   * The fully qualified name of the subtype.
   */
  public String value();
}
