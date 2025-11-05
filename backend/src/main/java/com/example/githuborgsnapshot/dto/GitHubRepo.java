package com.example.githuborgsnapshot.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GitHubRepo(
    String name,
    @JsonProperty("html_url") String htmlUrl,
    @JsonProperty("stargazers_count") Integer stargazersCount,
    @JsonProperty("forks_count") Integer forksCount,
    String language,
    @JsonProperty("updated_at") String updatedAt,
    String description
) {}
