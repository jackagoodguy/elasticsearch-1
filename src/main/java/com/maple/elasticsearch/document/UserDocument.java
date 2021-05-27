package com.maple.elasticsearch.document;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.maple.elasticsearch.config.ESField;
import com.maple.elasticsearch.config.EsIndex;
import com.maple.elasticsearch.model.ElasticBaseDocument;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(builderMethodName = "hiddenBuilder")
public class UserDocument implements ElasticBaseDocument {
  @ESField(type = "long")
  private Long id;
  @ESField(type = "keyword")
  private String userName;
  @ESField(type = "keyword")
  private String password;
  @ESField(type = "keyword")
  private String mobile;
  @ESField(type = "keyword")
  private String name;
  @ESField(type = "integer")
  private Integer age;
  @ESField(type = "integer")
  private Integer partition;

  public static UserDocumentBuilder builder(int partition) {
    return hiddenBuilder().partition(partition);
  }

  @Override
  @JsonIgnore
  public String get_id() {
    return id.toString();
  }

  @Override
  @JsonIgnore
  public String getIndex() {
    //自定义可以对索引进行拆分 但是查询的时候一定要带此字段,一般大数据量会用time来切分索引
    return EsIndex.USER.getCode() + "_" + partition;
  }
}
