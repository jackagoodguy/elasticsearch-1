package com.maple.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;

public class JsonUtils {
  private static JsonConverter converter = new JsonConverter(true);

  public static <M> M from(String value, Class<M> clazz) {
    return converter.from(value, clazz);
  }

  public static <M> M from(String value, TypeReference<M> clazz) {
    return converter.from(value, clazz);
  }

  public static <M> M fromOrException(String value, TypeReference<M> clazz) {
    return converter.fromOrException(value, clazz);
  }

  public static <M> M fromOrException(String value, Class<M> clazz) {
    return converter.fromOrException(value, clazz);
  }


  public static <M> M convertOrException(Object value, TypeReference<M> clazz) {
    return converter.convert(value, clazz);
  }

  public static String toString(Object obj) {
    return converter.to(obj);
  }

}
