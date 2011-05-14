package com.lemoulinstudio.small.apt.model;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used on methods of a network interface that should not be logged.
 * It overrides the @Log policies eventually applied on the type of the method's network interface.
 *
 * @author Vincent Cantin
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.METHOD})
public @interface NoLog {
  Side value() default Side.Both;
}
