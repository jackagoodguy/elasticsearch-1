package com.maple.elasticsearch.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by pengbo on 2018/12/20 20:28.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ESField {

  // a simple type like text, keyword, date, long, double, boolean or ip.
  // a type which supports the hierarchical nature of JSON such as object or nested.
  // or a specialised type like geo_point, geo_shape, or completion.
  String type() default "";

  // using for text type
  String analyzer() default "";

  // is need search
  boolean index() default true;

  String name() default "";

  String format() default "";

  boolean unwrapped() default false;

}
