package org.sms.io.common.utils;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DatabaseConfig {

    @JsonProperty("name")
    private final String name;

    @JsonProperty("driverClassName")
    private final String driverClassName;

    @JsonProperty("url")
    private final String url;

    @JsonProperty("username")
    private final String username;

    @JsonProperty("password")
    private final String password;

    public DatabaseConfig(@JsonProperty("name") String name,
                          @JsonProperty("driverClassName") String driverClassName,
                          @JsonProperty("url") String url,
                          @JsonProperty("username") String username,
                          @JsonProperty("password") String password) {
        this.name = name;
        this.driverClassName = driverClassName;
        this.url = url;
        this.username = username;
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public String getDriverClassName() {
        return driverClassName;
    }

    public String getUrl() {
        return url;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public String toString() {
        return "creating tenant for {" +
                "name='" + name + '\'' +
                ", driverClassName='" + driverClassName + '\'' +
                ", url='" + url + '\'' +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                '}';
    }
}
