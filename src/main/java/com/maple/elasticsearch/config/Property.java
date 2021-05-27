package com.maple.elasticsearch.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Map;

@Data
@Accessors(chain = true)
public class Property {

  private String type; // 字段类型, 必填

  private boolean index = true; // 该字段是否需要被索引,默认为true

  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private String analyzer; // 分词器

  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private String search_analyzer; // 搜索解析器

  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private String format;

  @JsonProperty("properties")
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private Map<String, Property> nestedProperties; // 嵌套结构, type=nested

  @JsonInclude(JsonInclude.Include.NON_NULL)
  public Boolean isIndex() {
    if ("nested".equals(type) || "object".equals(type)) {
      return null;
    }
    return index;
  }
}
