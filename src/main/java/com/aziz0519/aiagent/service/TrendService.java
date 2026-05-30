package com.aziz0519.aiagent.service;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import com.aziz0519.aiagent.repository.TrendTopicRepository;
import com.aziz0519.aiagent.repository.ScrapedPostRepository;
import com.aziz0519.aiagent.repository.TrendAnalysisRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import com.aziz0519.aiagent.model.TrendTopic;
import com.aziz0519.aiagent.model.TrendAnalysis;
import com.aziz0519.aiagent.model.Platform;

@Service
@RequiredArgsConstructor
public class TrendService {

    private final TrendTopicRepository trendTopicRepository;
    private final ScrapedPostRepository scrapedPostRepository;
    private final TrendAnalysisRepository trendAnalysisRepository;

    public List<TrendTopic> getLatestTrends() {
        final LocalDateTime since = LocalDateTime.now().minusHours(24);
        return this.trendTopicRepository.findByDetectedAtAfterOrderByTrendScoreDesc(since);
    }

    public List<TrendTopic> getTopTrends() {
        return this.trendTopicRepository.findTop20ByOrderByTrendScoreDesc();
            
    }

    public List<TrendTopic> getTrendsByCategory(final String category) {
        // Implement category-based retrieval logic
        return this.trendTopicRepository.findByCategoryOrderByTrendScoreDesc(category);
    }

    public List<TrendTopic> getTrendsByPlatform(final String platform) {
        // Implement platform-based retrieval logic
        return this.trendTopicRepository.findByPrimaryPlatformOrderByTrendScoreDesc(platform);
    }
    
    public Map<String, Object> getDashboardStats() {
        return Map.of(
            "totalPosts", this.scrapedPostRepository.count(),
            "redditPosts", this.scrapedPostRepository.countByPlatform(Platform.REDDIT),
            "hnPosts", this.scrapedPostRepository.countByPlatform(Platform.HACKERNEWS),
            "phPosts", this.scrapedPostRepository.countByPlatform(Platform.PRODUCTHUNT),
            "totalTrends", this.trendTopicRepository.count(),
            "lastAnalysis", this.trendAnalysisRepository.findTopByOrderByAnalyzedAtDesc()
                .map(TrendAnalysis::getAnalyzedAt)
                .orElse(LocalDateTime.now())
        );
    } 

}
