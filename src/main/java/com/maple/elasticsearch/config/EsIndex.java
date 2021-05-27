package com.maple.elasticsearch.config;

import com.maple.elasticsearch.document.UserDocument;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Created by maple on 2020/11/3
 * es索引
 */
@Getter
@Slf4j
@AllArgsConstructor
public enum EsIndex {
  USER("user", "用户信息", UserDocument.class);

  private final String code;
  private final String desc;
  private final Class documentClass;

  public String getCode() {
    return code;
  }
}
