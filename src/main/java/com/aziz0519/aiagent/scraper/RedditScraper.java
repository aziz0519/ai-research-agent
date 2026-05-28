package com.aziz0519.aiagent.scraper;

import java.util.List;
import com.aziz0519.aiagent.model.ScrapedPost;
import com.aziz0519.aiagent.model.Platform;

public class RedditScraper extends AbstractScraper implements PlatformScraper {

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
