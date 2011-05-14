package com.lemoulinstudio.small.apt.model;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used to tag network interfaces.
 *
 * This annotation is the only one required when designing a model.
 * All other ones are optional.
 *
 * @author Vincent Cantin
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.TYPE})
public @interface NetworkInterface {
  HostKind value();
}
