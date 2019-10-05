package com.github.mihone.redismq.config;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import redis.clients.jedis.Protocol;

public class RedisPoolConfig extends GenericObjectPoolConfig {

    private static final String DEFAULT_URL = "localhost";
    private static final Integer DEFAULT_PORT = 6379;
    private static final String DEFAULT_PASSWORD = null;
    private static final Integer DEFAULT_TIMEOUT = Protocol.DEFAULT_TIMEOUT;

    public static String getDefaultUrl() {
        return DEFAULT_URL;
    }

    public static Integer getDefaultPort() {
        return DEFAULT_PORT;
    }

    public static String getDefaultPassword() {
        return DEFAULT_PASSWORD;
    }

    public static Integer getDefaultTimeout() {
        return DEFAULT_TIMEOUT;
    }

    public static Integer getDefaultDatabase() {
        return DEFAULT_DATABASE;
    }

    private static final Integer DEFAULT_DATABASE = Protocol.DEFAULT_DATABASE;
    private String url = DEFAULT_URL;
    private Integer port = DEFAULT_PORT;
    private String password = DEFAULT_PASSWORD;
    private Integer timeout = DEFAULT_TIMEOUT;
    private Integer database = DEFAULT_DATABASE;

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
