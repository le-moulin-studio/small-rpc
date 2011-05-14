package com.lemoulinstudio.small.apt.model;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>This annotation is used on methods that should be logged.</p>
 *
 * <p>When used on a network interface type, it implicitly tags all its methods.</p>
 *
 * <p>When used with LogSide.Invocation as parameter, the methods tagged by
 * this annotation with be logged prior to be encoded by a SmallSession.</p>
 *
 * <p>When used with LogSide.Reception as parameter, the methods tagged by
 * this annotation with be logged after that reception of a message and before
 * the function is called by the smallSession</p>
 *
 * @author Vincent Cantin
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface Log {
  Side value() default Side.Both;
}
