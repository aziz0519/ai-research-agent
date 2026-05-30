package com.aziz0519.aiagent.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.aziz0519.aiagent.model.TrendAnalysis;

import java.util.Optional;

public interface TrendAnalysisRepository extends JpaRepository<TrendAnalysis, Long> {

    Optional<TrendAnalysis> findTopByOrderByAnalyzedAtDesc();

}
