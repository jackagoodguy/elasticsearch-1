package com.maple.elasticsearch;


import com.maple.elasticsearch.config.EsIndex;
import com.maple.elasticsearch.document.UserDocument;
import com.maple.elasticsearch.model.EsSearchResult;
import com.maple.elasticsearch.model.condition.UserEsCondition;
import com.maple.elasticsearch.model.impl.UserEsModel;
import com.maple.util.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;


@Slf4j
@SpringBootTest(classes = ElasticConfiguration.class)
class ElasticSearchServiceTest {
  @Autowired
  private ElasticSearchService elasticSearchService;
  @Autowired
  private UserEsModel userEsModel;


  @BeforeEach
  public void setUp() {
    clearES();
  }

  private void clearES() {
    try {
      List<String> indexes = elasticSearchService.listAllIndex();
      for (EsIndex index : EsIndex.values()) {
        for (String indexString : indexes) {
          if (indexString.startsWith(index.getCode()) && elasticSearchService.isIndexExists(indexString)) {
            elasticSearchService.deleteIndex(indexString);
          }
        }
      }
    } catch (Exception e) {
      log.error("clearES error ", e);
    }
  }

  @Test
  void listAllIndex() {
    log.info(JsonUtils.toString(elasticSearchService.listAllIndex()));
  }

  @Test
  void createDocument() {
    UserDocument user1 = UserDocument.builder(1).id(1L).name("张三").age(20).userName("jack").password("666").mobile("33232").build();
    UserDocument user2 = UserDocument.builder(1).id(2L).name("张三").age(30).userName("tom").password("666").mobile("33232").build();
    userEsModel.insert(user1);
    userEsModel.insert(user2);
    elasticSearchService.refreshEsIndexes();
    EsSearchResult<UserDocument> result = userEsModel.findByCondition(UserEsCondition.builder(1).build());
    //{"documents":[{"id":2,"userName":"tom","password":"666","mobile":"33232","name":"张三","age":30,"partition":1},{"id":1,"userName":"jack","password":"666","mobile":"33232","name":"张三","age":20,"partition":1}],"totalHits":2,"took":3}
    log.info(JsonUtils.toString(result));
    assertThat(2L, equalTo(result.getTotalHits()));
    //{"documents":[{"id":2,"userName":"tom","password":"666","mobile":"33232","name":"张三","age":30,"partition":1}],"totalHits":1,"took":96}
    result = userEsModel.findByCondition(UserEsCondition.builder(1).name("三").userName("tom").maxAge(30).build());
    log.info(JsonUtils.toString(result));
    assertThat(1L, equalTo(result.getTotalHits()));
  }
}