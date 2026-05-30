package com.aziz0519.aiagent.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import com.aziz0519.aiagent.service.ScrapingOrchestrator;
import com.aziz0519.aiagent.service.LlmAnalysisService; 
import com.aziz0519.aiagent.repository.ScrapedPostRepository;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.http.ResponseEntity;

import java.util.Map;
import java.util.List;
import com.aziz0519.aiagent.model.Platform;
import com.aziz0519.aiagent.model.TrendAnalysis;
import com.aziz0519.aiagent.model.ScrapedPost;
import java.time.LocalDateTime;




@RestController
@RequestMapping("/api/scrape")
@RequiredArgsConstructor
public class ScrapeController {

    private final ScrapingOrchestrator orchestrator;
    private final LlmAnalysisService analysisService;
    private final ScrapedPostRepository postRepository;

    @PostMapping("/run")
    public ResponseEntity<Map<String, Object>> triggerFullCycle() {
        final Map<Platform, Integer> scrapeResults = this.orchestrator.scrapeAll();
        final LocalDateTime since = LocalDateTime.now().minusHours(6);
        final List<ScrapedPost> posts = this.postRepository.findAllScrapedAtAfterOrderByScoreDesc(since);
        TrendAnalysis analysis = null;
        if (!posts.isEmpty()) {
            analysis = this.analysisService.analyze(posts);
        } 
        return ResponseEntity.ok(Map.of(
            "scrapeResults", scrapeResults,
            "analyzedPosts", posts.size(),
            "analysis", analysis
        ));
    }


}
