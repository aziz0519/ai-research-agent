package com.aziz0519.aiagent.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.aziz0519.aiagent.model.ScrapedPost;

public interface ScrapedPostRepository extends JpaRepository<ScrapedPost, Long> {

}
