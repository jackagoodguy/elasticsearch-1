package com.maple.elasticsearch.config;

import lombok.Data;
import lombok.experimental.Accessors;


// ES 的 索引
@Data
@Accessors(chain = true)
public class Index {

  private String name; // 名称, 必填

  private Setting setting; // 设置

  private Mapping mapping; // key 为 名称, value 为类型

    /*
    eg:

    Map<String, Property> properties = new HashMap<>();
    properties.put("id", new Property().setType("long").setIndex(true));
    properties.put("name", new Property().setType("keyword"));
    properties.put("story", new Property().setType("text").setAnalyzer("ik_max_word").setSearch_analyzer("ik_max_word"));

    Mapping mapping = new Mapping(properties);
    Index index = new Index().setName("test").setMapping(mapping);

    elasticSearchService.createIndex(index);

     */

}
