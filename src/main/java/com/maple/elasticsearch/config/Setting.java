package com.maple.elasticsearch.config;

import lombok.AllArgsConstructor;
import lombok.Data;

// ES 索引设置
@Data
@AllArgsConstructor
public class Setting {
    // 分片数量
    private Integer shards;

    // 备份数量
    private Integer replicas;
}
