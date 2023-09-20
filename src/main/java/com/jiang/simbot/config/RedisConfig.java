package com.jiang.simbot.config;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.core.StringRedisTemplate;

@Configuration
public class RedisConfig {
    @Value("${spring.redis.database.db0}")
    private int db0;

    @Value("${spring.redis.database.db1}")
    private int db1;

    @Value("${spring.redis.database.db2}")
    private int db2;

    @Value("${spring.redis.database.db3}")
    private int db3;

    @Value("${spring.redis.database.db4}")
    private int db4;
    @Value("${spring.redis.database.db5}")
    private int db5;
    @Value("${spring.data.redis.host}")
    private String host;


    @Value("${spring.data.redis.port}")
    private int port;


    @Bean
    public GenericObjectPoolConfig getPoolConfig() {
        // 配置redis连接池
        GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
        return poolConfig;
    }

    @Bean(name = "redisTemplate0")
    public StringRedisTemplate getRedisTemplate0() {
        return getStringRedisTemplate(db0);
    }

    @Bean(name = "redisTemplate1")
    public StringRedisTemplate getRedisTemplate1() {
        return getStringRedisTemplate(db1);
    }

    @Bean(name = "redisTemplate2")
    public StringRedisTemplate getRedisTemplate2() {
        return getStringRedisTemplate(db2);
    }
    @Bean(name = "redisTemplate3")
    public StringRedisTemplate getRedisTemplate3() {
        return getStringRedisTemplate(db3);
    }

    @Bean(name = "redisTemplate4")
    public StringRedisTemplate getRedisTemplate4() {
        return getStringRedisTemplate(db4);
    }
    @Bean(name = "redisTemplate5")
    public StringRedisTemplate getRedisTemplate5() {
        return getStringRedisTemplate(db5);
    }

    private StringRedisTemplate getStringRedisTemplate(int database) {
        // 构建工厂对象
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(host);
        config.setPort(port);
        //config.setPassword(RedisPassword.of(password));
        LettucePoolingClientConfiguration clientConfig = LettucePoolingClientConfiguration.builder()
                .poolConfig(getPoolConfig())
                .build();
        LettuceConnectionFactory factory = new LettuceConnectionFactory(config, clientConfig);
        // 设置使用的redis数据库
        factory.setDatabase(database);
        // 重新初始化工厂
        factory.afterPropertiesSet();
        return new StringRedisTemplate(factory);
    }

}
