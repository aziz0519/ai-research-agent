package com.aziz0519.aiagent.service;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.aziz0519.aiagent.model.ScrapedPost;
import java.util.List;
import com.aziz0519.aiagent.model.TrendAnalysis;

@Service
@RequiredArgsConstructor
@Slf4j
public class LlmAnalysisService {

    public TrendAnalysis analyze(final List<ScrapedPost> posts) {
        return null;
    
    };

}
