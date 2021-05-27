package com.maple.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.function.Supplier;


/**
 * Json 工具类，提供 Json序列化、反序列化、对象转化等方法。
 */
@Slf4j
public class JsonConverter {
  static class InstanceHolder {
    static JsonConverter instance = new JsonConverter(true);
  }

  /**
   * 建议使用 JsonUtils 而不是 JsonConverter 的单例
   */
  @Deprecated
  public static JsonConverter getInstance() {
    return InstanceHolder.instance;
  }

  private static final ObjectMapper mapper = new ObjectMapper();

  static {
    mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
  }

  JsonConverter(boolean logging) {
    this.logging = logging;
  }

  private final boolean logging;

  public <M> M from(String value, Class<M> clazz) {
    return exceptionToNull(() -> fromOrException(value, clazz));
  }

  public <M> M from(String value, TypeReference<M> typeReference) {
    return exceptionToNull(() -> fromOrException(value, typeReference));
  }


  public String to(Object obj) {
    return exceptionToNull(() -> toOrException(obj));
  }


  public <M> M convert(Object value, TypeReference<M> typeReference) {
    return mapper.convertValue(value, typeReference);
  }

  public <M> M fromOrException(String value, Class<M> clazz) {
    try {
      return mapper.readValue(value, clazz);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public <M> M fromOrException(String value, TypeReference<M> typeReference) {
    try {
      return mapper.readValue(value, typeReference);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public String toOrException(Object obj) {
    try {
      return mapper.writeValueAsString(obj);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  private <M> M exceptionToNull(Supplier<M> supplier) {
    try {
      return supplier.get();
    } catch (RuntimeException e) {
      doLogging(e);
      return null;
    }
  }

  private void doLogging(RuntimeException e) {
    if (logging) {
      log.error("", e);
    }
  }
}
