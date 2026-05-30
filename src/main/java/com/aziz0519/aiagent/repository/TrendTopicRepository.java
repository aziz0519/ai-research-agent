package com.aziz0519.aiagent.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.aziz0519.aiagent.model.TrendTopic;

import java.time.LocalDateTime;
import java.util.List;

public interface TrendTopicRepository extends JpaRepository<TrendTopic, Long> {
    List<TrendTopic> findByDetectedAtAfterOrderByTrendScoreDesc(LocalDateTime since);

    List<TrendTopic> findTop20ByOrderByTrendScoreDesc();

    List<TrendTopic> findByCategoryOrderByTrendScoreDesc(String category);

    List<TrendTopic> findByPrimaryPlatformOrderByTrendScoreDesc(String platform);

}
