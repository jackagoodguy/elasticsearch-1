package com.maple.elasticsearch.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * Created by maple on 2021/1/21
 */
@Data
@Component
public class ElasticConfig {
  private int connectTimeOut = 1000; // 连接超时时间
  private int socketTimeOut = 30000; // 连接超时时间
  private int connectionRequestTimeOut = 500; // 获取连接的超时时间
  private int maxConnectTotal = 100; // 最大连接数
  private int maxConnectPerRoute = 100; // 最大路由连接数
  private String username = "";
  private String password = "";
  private boolean esSwitch = false;
  private boolean esModelLogSwitch = true;
  private Integer shards = 1;
  private Integer replicas = 1;
  private Integer updateRetryOnConflict = 3;
  private boolean trackTotalHits = true;
  private Integer maxResultWindow = 30000;

  public List<HostConfigVO> getHostConfigs() {
    return Arrays.asList(new HostConfigVO("localhost", 9200, "http"));
  }


  @Data
  @AllArgsConstructor
  public static class HostConfigVO {
    private String host;
    private Integer port;
    private String schema;
  }

}
