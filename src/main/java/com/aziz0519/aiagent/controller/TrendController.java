package com.aziz0519.aiagent.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aziz0519.aiagent.service.TrendService;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.PathVariable;

import com.aziz0519.aiagent.model.TrendTopic;


@RestController
@RequestMapping("/api/trends")
@RequiredArgsConstructor
public class TrendController {

    private final TrendService trendService;

    @GetMapping
    public ResponseEntity<List<TrendTopic>> getTopTrends() {
        return ResponseEntity.ok(this.trendService.getTopTrends());
    }

    @GetMapping("/latest")
    public ResponseEntity<List<TrendTopic>> getLatestTrends() {
        return ResponseEntity.ok(this.trendService.getLatestTrends());

    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<TrendTopic>> getTrendsbyCategory(
        @PathVariable
        final String category
    ) {
        return ResponseEntity.ok(this.trendService.getTrendsByCategory(category));
    }

    @GetMapping("/platform/{platform}")
    public ResponseEntity<List<TrendTopic>> getTrendsbyPlatform(
        @PathVariable
        final String platform
    ) {
        return ResponseEntity.ok(this.trendService.getTrendsByPlatform(platform));
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        return ResponseEntity.ok(this.trendService.getDashboardStats());
    }



}
