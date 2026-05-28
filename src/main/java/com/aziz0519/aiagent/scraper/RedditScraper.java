package com.aziz0519.aiagent.scraper;

import java.util.List;

import org.springframework.stereotype.Component;

import com.aziz0519.aiagent.model.ScrapedPost;

import lombok.extern.slf4j.Slf4j;

import com.aziz0519.aiagent.model.Platform;

import com.aziz0519.aiagent.config.ProxyConfig;

@Component
@Slf4j
public class RedditScraper extends AbstractScraper implements PlatformScraper {

    public RedditScraper(final ProxyConfig proxyConfig) {
        super(proxyConfig);
    }

    @Override
    public Platform getPlatform() {
        return Platform.REDDIT;
    }

    @Override
    public List<ScrapedPost> scraped() {
        return List.of(
            new ScrapedPost("Reddit Post 1", "This is the content of Reddit Post 1"),
            new ScrapedPost("Reddit Post 2", "This is the content of Reddit Post 2")
        );
    
    }

}
