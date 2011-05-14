package com.lemoulinstudio.small.apt.model;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Vincent Cantin
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.TYPE})
public @interface BindToSharedId {
}
