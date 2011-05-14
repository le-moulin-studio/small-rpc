package com.lemoulinstudio.small.apt.model;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to tag a parameter which will have its value injected with the
 * caller object on the receiver side. The annotated parameter will not appear
 * on the generated local interface on the caller side.
 *
 * @author Vincent Cantin
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.PARAMETER})
public @interface CallerObject {
}
