package com.aziz0519.aiagent.scraper;

import java.util.List;

import org.springframework.stereotype.Component;

import com.aziz0519.aiagent.config.ProxyConfig;
import com.aziz0519.aiagent.model.Platform;
import com.aziz0519.aiagent.model.ScrapedPost;


import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;





import java.time.LocalDateTime;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;

import org.json.JSONObject;
import org.json.JSONArray;


import com.aziz0519.aiagent.repository.ScrapedPostRepository;




@Component
@Slf4j
@Setter
@Getter
public class RedditScraper extends AbstractScraper implements PlatformScraper {

    private final ScrapedPostRepository scrapedPostRepository;


    public RedditScraper(final ProxyConfig proxyConfig,
                            final ScrapedPostRepository scrapedPostRepository
    ) {
        super(proxyConfig);
        this.scrapedPostRepository = scrapedPostRepository;

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

                final JSONObject root = new JSONObject(json);

                final JSONArray children = root.getJSONObject("data").getJSONArray("children");

                for (int i = 0; i < children.length(); i++) {
                    JSONObject data = children.getJSONObject(i).getJSONObject("data");

                    final String externalId = data.optString("id", "");
  
                    
                    if (externalId.isBlank()) {
                        continue;
                    }

                    if (this.scrapedPostRepository.existsByExternalIdAndPlatform(externalId, getPlatform())) {
                        continue;
                    }

                    final String title = data.optString("title", "");
                    if (title.isBlank()) {
                        continue;
                    }

                    final String selftext = data.optString("selftext", "").trim();
                    
                    final String content = selftext.isBlank() 
                        ? title.substring(0, Math.min(title.length(), 500)) 
                        : selftext;

                    final long postedAtEpoch = data.optLong("created_utc", 0);

                    final LocalDateTime postedAt = postedAtEpoch > 0
                        ? LocalDateTime.ofInstant(Instant.ofEpochSecond(postedAtEpoch), ZoneId.systemDefault())
                        : null;

                    final String redditUrl = data.optString("url", "");

                    final String author = data.optString("author", "");

                    final int score = data.optInt("score", 0);

                    final int commentCount = data.optInt("num_comments", 0);
                    final String subredditName = data.optString("subreddit", subreddit);

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

                Thread.sleep(500); // Sleep to respect Reddit's rate limits

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;

                } catch (final Exception e) {
                    log.error("Unexpected error while scraping r/{}", subreddit, e.getMessage());
                }
            
        }


        return posts;
        
    }
    
}
