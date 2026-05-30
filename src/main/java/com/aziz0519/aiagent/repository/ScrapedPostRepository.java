package com.aziz0519.aiagent.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.aziz0519.aiagent.model.ScrapedPost;

import com.aziz0519.aiagent.model.Platform;
public interface ScrapedPostRepository extends JpaRepository<ScrapedPost, Long> {

    boolean existsByExternalIdAndPlatform(String externalId, Platform platform);

    Object countByPlatform(Platform platform);



}
