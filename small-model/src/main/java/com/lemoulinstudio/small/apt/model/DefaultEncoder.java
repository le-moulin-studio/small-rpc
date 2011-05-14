package com.lemoulinstudio.small.apt.model;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 *
 * @author Vincent Cantin
 */
@Retention(RetentionPolicy.SOURCE)
public @interface DefaultEncoder {
  // The type of the target encoder.
  Class type();
  
  // The name of this encoder.
  String name();
}
