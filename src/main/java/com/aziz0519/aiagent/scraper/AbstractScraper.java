package com.aziz0519.aiagent.scraper;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Component;

import com.aziz0519.aiagent.config.ProxyConfig;

import lombok.extern.slf4j.Slf4j;
import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

@Component
@Slf4j
public class AbstractScraper {

    private final ProxyConfig proxyConfig;

    public AbstractScraper(ProxyConfig proxyConfig) {
        this.proxyConfig = proxyConfig;
    }

    public OkHttpClient okHttpClient() {
        return new OkHttpClient.Builder()
                // Configure proxy settings if needed
                .proxy(this.proxyConfig.toProxy())
                .proxyAuthenticator((route, response) ->
                        response.request().newBuilder()
                                .header("Proxy-Authorization", Credentials.basic(this.proxyConfig.getUsername(), this.proxyConfig.getPassword()))
                                .build())
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    public String fetch(final String url) throws IOException {
        final Request request = new Request.Builder()
                .url(url)
                .header("User-Agent", this.proxyConfig.getUserAgent())
                .build();
        try (Response response = okHttpClient().newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("HTTP code " + response.code() + " for " + url);
            }
            assert response.body() != null;
            return Objects.requireNonNull(response.body()).string();
        } 
    }

    public String detectProxyIp() {
        try {
            final String body = fetch("https://api.ipify.org");
            return body.trim();
        } catch (IOException e) {
            log.debug("Failed to detect proxy IP", e);
            return "Unknown";
        }

    }

}
