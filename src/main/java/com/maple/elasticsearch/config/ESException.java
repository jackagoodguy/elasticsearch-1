package com.maple.elasticsearch.config;

public class ESException extends RuntimeException {
  public ESException() {
  }

  public ESException(String message) {
    super(message);
  }

  public ESException(String message, Throwable cause) {
    super(message, cause);
  }
}
