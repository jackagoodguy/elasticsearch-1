package com.maple.elasticsearch.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Mapping {
  private Map<String, Property> properties = new HashMap<>();
}
