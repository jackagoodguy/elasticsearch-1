package com.maple.elasticsearch;

import com.maple.elasticsearch.config.ElasticConfig;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Created by maple on 2021/1/21
 */
@Configuration
@ComponentScan(basePackages = {"com.maple.elasticsearch"})
public class ElasticConfiguration {

  @Autowired
  private ElasticConfig esConfig;


  @Bean
  public RestClientBuilder restClientBuilder() {
    List<ElasticConfig.HostConfigVO> hostConfigs = esConfig.getHostConfigs();
    HttpHost[] hosts = new HttpHost[hostConfigs.size()];
    for (int i = 0; i < hostConfigs.size(); i++) {
      ElasticConfig.HostConfigVO hostConfig = hostConfigs.get(i);
      HttpHost host = new HttpHost(hostConfig.getHost(), hostConfig.getPort(), hostConfig.getSchema());
      hosts[i] = host;
    }
    return RestClient.builder(hosts);
  }

  @Bean
  public RestHighLevelClient getRestHighLevelClient(@Autowired RestClientBuilder restClientBuilder) {
    restClientBuilder.setHttpClientConfigCallback(httpClientBuilder -> {
      httpClientBuilder.setMaxConnTotal(esConfig.getMaxConnectTotal());
      httpClientBuilder.setMaxConnPerRoute(esConfig.getMaxConnectPerRoute());
      final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
      credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(esConfig.getUsername(), esConfig.getPassword())); //用户名和密码用于鉴权
      httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
      return httpClientBuilder;
    });
    restClientBuilder.setRequestConfigCallback(requestConfigBuilder -> {
      requestConfigBuilder.setConnectTimeout(esConfig.getConnectTimeOut());
      requestConfigBuilder.setSocketTimeout(esConfig.getSocketTimeOut());
      requestConfigBuilder.setConnectionRequestTimeout(esConfig.getConnectionRequestTimeOut());
      return requestConfigBuilder;
    });
    return new RestHighLevelClient(restClientBuilder);
  }
}
