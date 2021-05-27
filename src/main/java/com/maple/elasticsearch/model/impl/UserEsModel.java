package com.maple.elasticsearch.model.impl;

import com.maple.elasticsearch.config.EsIndex;
import com.maple.elasticsearch.document.UserDocument;
import com.maple.elasticsearch.model.BaseEsModel;
import org.springframework.stereotype.Component;

@Component
public class UserEsModel extends BaseEsModel<UserDocument> {
  public UserEsModel() {
    super(UserDocument.class, EsIndex.USER);
  }
}
