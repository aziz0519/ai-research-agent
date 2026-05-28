package com.aziz0519.aiagent.scraper;

import okhttp3.OkHttpClient;

public class AbstractScraper {

    public OkHttpClient okHttpClient() {
        return new OkHttpClient();
    }

    public String fetch(final String url) {
        // Placeholder for HTTP fetching logic
        return "Fetched content from " + url;
    }

    public String detectProxyIp() {
        // Placeholder for proxy IP detection logic
        return "127.0.0.1";
    }

}
