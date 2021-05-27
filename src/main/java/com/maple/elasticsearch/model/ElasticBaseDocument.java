package com.maple.elasticsearch.model;

public interface ElasticBaseDocument {

  String get_id();

  String getIndex();

  default void set_id(String _id) {
  }

}
