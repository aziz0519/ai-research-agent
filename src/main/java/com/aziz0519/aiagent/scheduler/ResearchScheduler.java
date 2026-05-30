package com.aziz0519.aiagent.scheduler;

import com.aziz0519.aiagent.scraper.AbstractScraper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.aziz0519.aiagent.service.ScrapingOrchestrator;
import com.aziz0519.aiagent.repository.ScrapedPostRepository;
import com.aziz0519.aiagent.service.LlmAnalysisService;

import com.aziz0519.aiagent.model.Platform;

import java.util.Map;
import java.util.List;
import java.time.LocalDateTime;

import com.aziz0519.aiagent.model.ScrapedPost;

@Component
@RequiredArgsConstructor
@Slf4j
public class ResearchScheduler {

    private final AbstractScraper abstractScraper;
    private final ScrapingOrchestrator scrapingOrchestrator;
    private final ScrapedPostRepository postRepository;
    private final LlmAnalysisService analysisService;


    ResearchScheduler(AbstractScraper abstractScraper) {
        this.abstractScraper = abstractScraper;
    }


    @Scheduled(cron = "${scraping.cron}") // Every hour
    public void runResearchCycle() {
        log.info("===== Research cycle started =====");
        
        final Map<Platform, Integer> results = this.scrapingOrchestrator.scrapeAll();

        log.info("Scraping results: {}", results);

        final LocalDateTime since = LocalDateTime.now().minusHours(6);

        final List<ScrapedPost> recentPosts = this.postRepository.findAllScrapedAtAfterOrderByScoreDesc(since);

        if (!recentPosts.isEmpty()) {
            this.analysisService.analyze(recentPosts);
            log.info("LLM analysis completed for {} posts.", recentPosts.size());
        } 
        log.info("===== Research cycle completed =====");
    }

}
