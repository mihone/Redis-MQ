package com.mihone.redismq.config;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import redis.clients.jedis.Protocol;

public class RedisPoolConfig extends GenericObjectPoolConfig {

    public static final String DEFAULT_URL = "localhost";
    public static final Integer  DEFAULT_PORT= 6379;
    public static final String DEFAULT_PASSWORD = null;
    public static final Integer DEFAULT_TIMEOUT = Protocol.DEFAULT_TIMEOUT;
    public static final Integer DEFAULT_DATABASE = Protocol.DEFAULT_DATABASE;
    private String url=DEFAULT_URL;
    private Integer port=DEFAULT_PORT;
    private String password=DEFAULT_PASSWORD;
    private Integer timeout=DEFAULT_TIMEOUT;
    private Integer database=DEFAULT_DATABASE;

    public Integer getDatabase() {
        return database;
    }

    public void setDatabase(Integer database) {
        this.database = database;
    }

    public Integer getTimeout() {
        return timeout;
    }

    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
