package com.aziz0519.aiagent.scraper;

import java.util.List;

import org.springframework.stereotype.Component;

import com.aziz0519.aiagent.config.ProxyConfig;
import com.aziz0519.aiagent.model.Platform;
import com.aziz0519.aiagent.model.ScrapedPost;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.Entity;

import java.time.LocalDateTime;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;



@Component
@Slf4j
@Setter
@Getter
@Entity
public class RedditScraper extends AbstractScraper implements PlatformScraper {

    private final ScrapedPostRepository scrapedPostRepository;
    private final ObjectMapper objectMapper;

    public RedditScraper(final ProxyConfig proxyConfig,
                            final ScrapedPostRepository scrapedPostRepository,
                            final ObjectMapper objectMapper
    ) {
        super(proxyConfig);
        this.scrapedPostRepository = scrapedPostRepository;
        this.objectMapper = objectMapper;
    }

    @Value("${scraping.reddit.subreddits}")
    private List<String> subreddits;

    @Value("${scraping.reddit.postsPerSubreddit}")
    private int postsPerSubreddit;

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

                    final String externalId = data.path("id").asText();
                    
                    if (externalId.isBlank()) {
                        continue;
                    }

                    if (this.scrapedPostRepository.existsByExternalIdAndPlatform(getPlatform(), externalId)) {
                        continue;
                    }

                    final String title = data.path("title").asText(defaultValue: "");
                    if (title.isBlank()) {
                        continue;
                    }

                    final String selftext = data.path("selftext")
                            .asText(defaultValue: "")
                            .trim();
                    
                    final String content = selftext.isBlank() 
                        ? title.substring(0, Math.min(title.length(), 500)) 
                        : selftext;

                    final long postedAtEpoch = (long) data.path("created_utc")
                    .asDouble();

                    final LocalDateTime postedAt = data.has("created_utc") 
                        ? LocalDateTime.ofInstant(
                        Instant.ofEpochSecond(postedAtEpoch), 
                        ZoneId.systemDefault()) 
                        : null;

                    final String redditUrl = data.path("url")
                        .asText(defaultValue: "null");
                    final String author = data.path("author")
                        .asText(defaultValue: "null");
                    final int score = data.path("score")
                        .asInt(0);
                    final int commentCount = data.path("num_comments").asInt(0);
                    final String subredditName = data.path("subreddit").asText(subreddit);

                    final ScrapedPost post = ScrapedPost.builder()
                            .externalId(externalId)
                            .platform(getPlatform())
                            .title(title)
                            .content(content)
                            .postedAt(postedAt)
                            .url(redditUrl)
                            .author(author)
                            .score(score)
                            .commentCount(commentCount)
                            .subReddit(subredditName)
                            .proxyIpUsed(proxyIp)
                            .build();

                    posts.add(post);

                }
                log.info("Reddit r/{} scraped: {} new posts", subreddit, posts.size());

                Thread.sleep(500);

            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
                break;

                } catch (final Exception e) {
                    log.error("Failed to scrape r/{}", subreddit, e.getMessage());


                }
            
        }


        return posts;
        
    }
    
}
