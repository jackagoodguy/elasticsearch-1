package com.maple.elasticsearch.model.condition;

import com.maple.elasticsearch.config.EsIndex;
import com.maple.elasticsearch.model.AbstractBaseEsCondition;
import lombok.Builder;
import lombok.Data;
import org.apache.logging.log4j.util.Strings;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.util.StringUtils;

@Data
@Builder(builderMethodName = "hiddenBuilder")
public class UserEsCondition extends AbstractBaseEsCondition {
  private Long id;
  private String name;
  private Integer minAge;
  private Integer maxAge;
  private String userName;
  private int partition;

  public static UserEsConditionBuilder builder(int partition) {
    return hiddenBuilder().partition(partition);
  }


  @Override
  public QueryBuilder getQueryBuilder() {
    BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
    if (maxAge != null) {
      queryBuilder.filter(QueryBuilders.rangeQuery("age").lte(maxAge));
    }
    if (minAge != null) {
      queryBuilder.filter(QueryBuilders.rangeQuery("age").gte(minAge));
    }
    if (Strings.isNotBlank(userName)) {
      queryBuilder.filter(QueryBuilders.termQuery("userName", userName));
    }
    if (Strings.isNotBlank(name)) {
      queryBuilder.filter(QueryBuilders.wildcardQuery("name", "*" + name + "*"));
    }
    return queryBuilder;
  }

  @Override
  public String getIndex() {
    return EsIndex.USER.getCode() + "_" + partition;
  }
}
