package com.maple.elasticsearch.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Created by maple on 2021/1/27
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class EsSearchResult<V> {
  /**
   * 返回文档集合
   */
  private List<V> documents;
  /**
   * 命中总数
   */
  private Long totalHits;
  /**
   * 查询耗时 单位毫秒
   */
  private Long took;

}
