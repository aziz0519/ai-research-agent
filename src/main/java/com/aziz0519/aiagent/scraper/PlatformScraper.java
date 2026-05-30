package com.aziz0519.aiagent.scraper;

import java.util.List;

import com.aziz0519.aiagent.model.Platform;
import com.aziz0519.aiagent.model.ScrapedPost;

public interface PlatformScraper {

    Platform getPlatform();

    List<ScrapedPost> scrape();

}
