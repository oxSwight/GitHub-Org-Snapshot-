package com.example.githuborgsnapshot.dto;

public record RepoDto(
    String name,
    String htmlUrl,
    Integer stargazersCount,
    Integer forksCount,
    String language,
    String updatedAt,
    String description
) {
  public static RepoDto fromGitHub(GitHubRepo r) {
    return new RepoDto(
        r.name(),
        r.htmlUrl(),
        r.stargazersCount(),
        r.forksCount(),
        r.language(),
        r.updatedAt(),
        r.description()
    );
  }
}
