package com.aziz0519.aiagent.service;

import com.aziz0519.aiagent.config.OpenAIConfig;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.aziz0519.aiagent.model.Platform;
import com.aziz0519.aiagent.model.ScrapedPost;
import com.aziz0519.aiagent.model.TrendAnalysis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


import com.aziz0519.aiagent.repository.TrendAnalysisRepository;
import com.aziz0519.aiagent.repository.TrendTopicRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.springframework.web.reactive.function.client.WebClient;
import com.aziz0519.aiagent.model.TrendTopic;
import com.fasterxml.jackson.databind.JsonNode;

@Service
@RequiredArgsConstructor
@Slf4j
public class LlmAnalysisService {

    private final OpenAIConfig openAIConfig;

    private static final String SYSTEM_PROMPT = """
    You are an AI research analyst specialzing in technology trends and startup ideas.

    You will receive a batch of posts from Reddit, Hacker News and Product Hunt.

    Analyze them and identify the top emerging trends, recurring topics, and notable signals.

    For each trend you identify, respond with only a JSON array (no markdown, no preamble):

    [
        {
            "topic": "Short topic name",
            "summary": "2-3 sentence summary and why it's trending now",
            "reasoning": "Why is this significant and why is it trending now?",
            "category": "One of: AI/ML, DevTools, SaaS, Infrastructure, Security, Web3, Hardware, Other",
            "mentionCount": 5,
            "trendScore": 0.05,
            "primaryPlatform": "REDDIT",
            "relatedPostIds": ["ext_id_1","ext_id_2"]

        }
    ]
    
    Rules:
        - Identify 5-10 trends maximum
        - trendScore is 0.0 to 1.0
        - mentionCount = how many posts in this batch related to this trend
        - Only include trends mentioned in 2+ posts OR with very high engagement score (score > 200)
        - Focus on EMERGING trends, not well-established topics
        - primaryPlatform = where the trend is the strongest
    """;

    private final WebClient openAIWebClient;
    private final TrendAnalysisRepository analysisRepository;
    private final TrendTopicRepository topicRepository;
    private final ObjectMapper objectMapper;

    @Value("${openai.model}")
    private String model;

    LlmAnalysisService(OpenAIConfig openAIConfig) {
        this.openAIConfig = openAIConfig;
    }

    public TrendAnalysis analyze(final List<ScrapedPost> posts) {

        final String userPrompt = buildPrompt(posts);
        String rawResponse = null;

        for (int attempt = 1; attempt <= 3; attempt++) {
            try {
                Map<String, Object> requestBody = Map.of(
                    "model", model,
                    "messages", List.of(
                        Map.of("role", "system", "content", SYSTEM_PROMPT),
                        Map.of("role", "user", "content", userPrompt)
                    )
                );
                rawResponse = this.openAIWebClient.post()
                    .uri("/messages")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
                break; // Exit loop if successful
            } catch (Exception e) {
                log.warn("OpenAI attempt {}/3 failed: {}", attempt, e.getMessage());
                if (attempt < 3) {
                    try {
                        Thread.sleep(2000); // Exponential backoff
                    } catch (final InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        log.error("Retry sleep interrupted: {}", ie.getMessage());
                    }
                } 
    
            }
            if (rawResponse == null) {
                log.error("All OpenAI attempts failed to respond.");
                rawResponse = "{}";
            }
            TrendAnalysis analysis = TrendAnalysis.builder()
                .rawAnalysis(rawResponse)
                .postsAnalysis(posts.size())
                .build();
            analysis = this.analysisRepository.save(analysis);

            final List<TrendTopic> topics = parseTopics(rawResponse, analysis);
            this.topicRepository.saveAll(topics);
            log.info("Saved {} trend topics from analysis of {} posts", topics.size(), posts.size());
        }
        return analysis;
    
    };

    private String buildPrompt(final List<ScrapedPost> posts) {

        final StringBuilder prompt = new StringBuilder("Here are latest posts from the tech communities: \n\n");
        for (final ScrapedPost post: posts) {
            prompt.append(String.format("[%s] (score: %d, comments: %d) %s%n)",
                        post.getPlatform(),
                        post.getScore(),
                        post.getCommentCount(),
                        post.getTitle()
                        ));
            if (post.getContent() != null && !post.getContent().isBlank()) {
                
                final String snippet = post.getContent().length() > 200
                        ? post.getContent().substring(0, 200) + "..."
                        : post.getContent();
            
                prompt.append(" > ").append(snippet).append("\n");
            }
            prompt.append("\n");
        }
        return prompt.toString();
    }

    private List<TrendTopic> parseTopics(final String rawResponse, final TrendAnalysis analysis) {
        final List<TrendTopic> topics = new ArrayList<>();
        try {
            final JsonNode root = this.objectMapper.readTree(rawResponse);

            final JsonNode contentArray = root.path("content");
            if (!contentArray.isArray() || contentArray.isEmpty()) {
                log.error("Unexpected OpenAI response structure: {}", rawResponse);
                return topics;
            }
            String content = contentArray.get(0).path("text").asText().strip();

            if (content.startsWith("````")) {
                content = content.replaceFirst("^```[a-zA-Z]*\\n", "");
                content = content.replaceFirst("^```$\\n", "").strip();

            }
            
            final JsonNode trendsArray = this.objectMapper.readTree(content);
            if (!trendArray.isArray()) {
                return topics;

            }
            for (final JsonNode node: trendsArray) {
                try {
                    final Platform platform = parsePlatform(node.path("primaryPlatform").asText());
                    final List<String> relatedIds = new ArrayList<>();
                    final JsonNode relatedNode = node.path("relatedpostIds");
                    if (relatedNode.isArray()) {
                        for (final JsonNode id : relatedNode) {
                            relatedIds.add(id.asText());
                        }
                    }
                
                final TrendTopic topic = TrendTopic.builder()
                    .topic(node.path("topic").asText(""))
                    .summary(node.path("summary").asText(""))
                    .reasoning(node.path("reasoning").asText(""))
                    .category(node.path("category").asText(""))
                    .mentionCount(node.path("mentionCount").asInt(0))
                    .trendScore(node.path("trendScore").asDouble(0.0))
                    .primaryPlatform(platform)
                    .samplePostIds(String.join(",", relatedIds))
                    .analysis(analysis)
                    .build();

                topics.add(topic);

                } catch (final Exception e) {
                    log.error("Failed to parse trend topic: {}", node, e);

                }
            }
        } catch (final Exception e) {
            log.error("Failed to parse OpenAI response: {}", rawResponse, e);
        }
        return topics;
    }

    private Platform parsePlatform(final String primaryPlatform) {
        try {
            return Platform.valueOf(primaryPlatform.toUpperCase());
        } catch (final Exception e) {
            return null;
        }
    }

}
