package com.aziz0519.aiagent.config;

import java.net.InetSocketAddress;
import java.net.Proxy;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Configuration
@ConfigurationProperties(prefix = "proxy")
@Getter
@Setter
@Slf4j
public class ProxyConfig {
    private String host;
    private int port;
    private String username;
    private String password;
    private String userAgent;


    public Proxy toProxy() {
        log.info("Configuring proxy with host: {}, port: {}", host, port);
        return new Proxy(Proxy.Type.HTTP, new InetSocketAddress(host, port));
    }
}
