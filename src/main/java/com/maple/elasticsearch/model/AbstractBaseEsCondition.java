package com.maple.elasticsearch.model;

import lombok.Data;
import org.apache.logging.log4j.util.Strings;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;

/**
 * Created by maple on 2021/1/25
 */
@Data
public abstract class AbstractBaseEsCondition {

  private Integer pageSize = 10;
  private Integer pageNo = 1;
  private String orderField = "_id";
  private SortOrder orderType = SortOrder.DESC;

  public Integer getFrom() {
    return pageNo != null && pageSize != null ? (pageNo - 1) * pageSize : null;
  }

  public Integer getSize() {
    return pageSize;
  }

  public abstract QueryBuilder getQueryBuilder();

  public abstract String getIndex();

  public SearchSourceBuilder getSearchSourceBuilder() {
    SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder().query(getQueryBuilder());
    if (getFrom() == null && getSize() == null) {
      searchSourceBuilder.size(1000);
    } else {
      if (getFrom() != null) {
        searchSourceBuilder.from(getFrom());
      }
      if (getSize() != null) {
        searchSourceBuilder.size(getSize());
      }
    }
    if (Strings.isNotBlank(getOrderField())) {
      searchSourceBuilder.sort(getOrderField(), getOrderType());
    }
    return searchSourceBuilder;
  }

  public BoolQueryBuilder createNestBoolBuilder(BoolQueryBuilder queryBuilder) {
    BoolQueryBuilder nestBuilder = QueryBuilders.boolQuery();
    queryBuilder.filter(nestBuilder);
    return nestBuilder;
  }
}
