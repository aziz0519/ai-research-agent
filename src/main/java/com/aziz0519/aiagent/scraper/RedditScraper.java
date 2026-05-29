package com.aziz0519.aiagent.scraper;

import java.util.List;

import org.springframework.stereotype.Component;

import com.aziz0519.aiagent.config.ProxyConfig;
import com.aziz0519.aiagent.model.Platform;
import com.aziz0519.aiagent.model.ScrapedPost;

import lombok.Builder;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.Entity;

import java.time.LocalDateTime;
import java.time.Instant;

@Component
@Slf4j
@Setter
@Builder
@Entity
public class RedditScraper extends AbstractScraper implements PlatformScraper {

    private final ScrapedPostRepository scrapedPostRepository;
    private final ObjectMapper objectMapper;

    public RedditScraper(final ProxyConfig proxyConfig,
                            final ScrapedPostRepository scrapedPostRepository,
                            final ObjectMapper objectMapper
    ) {
        super(proxyConfig);
        this.postRepository = postRepository;
        this.objectMapper = objectMapper;
    }

    @Value('${scraping.reddit.subreddits}')
    private List<String> subreddits;

    @Value('${scraping.reddit.postsPerSubreddit}')
    private int postsPerSubreddit = 10;

    @Override
    public Platform getPlatform() {
        return Platform.REDDIT;
    }

    @Override
    public List<ScrapedPost> scrape() {
    
        // Implementation for scraping Reddit posts
        final List<ScrapedPost> posts = new ArrayList<>();

        log.info("Reddit scraper started");

        log.info("Reddit scraper using subreddits: {}", this.subreddits);

        for (final String subreddit : this.subreddits) {
            try {
                final String url = "https://reddit.com/r/" + subreddit + "/hot.json?limit=" + this.postsPerSubreddit;

                final String json = fetch(url);

                final String proxyIp = detectProxyIp();

                final JsonNode root = this.objectMapper.readTree(json);

                final JsonNode children = root.path("data").path("children");

                for (final JsonNode child : children) {
                    final JsonNode data = child.path("data");

                    final String exeternaId = data.path("id").asText();
                    
                    if (externalId.isBlank()) {
                        continue;
                    }

                    if (this.postRepository.existsByExternalIdAndPlatform(getPlatform(), externalId)) {
                        continue;
                    }

                    final String title = data.path("title").asText(defaultValue: "");
                    if (title.isBlank()) {
                        continue;
                    }

                    final String selftext = data.path("selftext")
                            .asText(defaultValue: "")
                            .trim();
                    
                    final String content = selftext.isBlank() ? title.substring(0, Math.min(title.length(), 200)) : selftext;
                    final double postedAtEpoch = data.path("created_utc").asDouble(0);

                    final LocalDateTime postedAt = data.has("created_utc") ? LocalDateTime.ofInstant(Instant.ofEpochSecond(data.path("created_utc").asLong()), ZoneId.systemDefault()) : LocalDateTime.now();

                } catch (final Exception e) {

                }
            
        }


        return List.of();
        
    }
    
}
