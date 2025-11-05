// backend/src/main/java/com/example/githuborgsnapshot/service/GitHubService.java
package com.example.githuborgsnapshot.service;

import com.example.githuborgsnapshot.dto.GitHubRepo;
import com.example.githuborgsnapshot.dto.RepoDto;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class GitHubService {

  enum SortBy { STARS, UPDATED }

  private final WebClient github;

  public GitHubService(WebClient githubWebClient) {
    this.github = githubWebClient;
  }

  public List<RepoDto> getTopRepos(String org, String sortParam, int limit) {
    SortBy sort = normalizeSort(sortParam);
    int safeLimit = clamp(limit, 1, 20);

    List<GitHubRepo> repos = getRepos(org);

    Comparator<GitHubRepo> comparator = (sort == SortBy.UPDATED)
            ? Comparator.comparing((GitHubRepo r) -> parseInstantSafe(r.updatedAt())).reversed()
            : Comparator.comparing(GitHubRepo::stargazersCount, Comparator.nullsFirst(Comparator.naturalOrder())).reversed();

    return repos.stream()
            .filter(Objects::nonNull)
            .sorted(comparator)
            .limit(safeLimit)
            .map(RepoDto::fromGitHub)
            .collect(Collectors.toList());
  }

  @Cacheable(cacheNames = "repos", key = "#org")
  public List<GitHubRepo> getRepos(String org) {
    List<GitHubRepo> fromOrg = fetch("/orgs/{org}/repos?per_page=100", org);
    if (fromOrg != null) return fromOrg;

    List<GitHubRepo> fromUser = fetch("/users/{org}/repos?per_page=100", org);
    if (fromUser != null) return fromUser;

    throw new ResponseStatusException(HttpStatus.NOT_FOUND,
            "Organization or user '" + org + "' not found on GitHub.");
  }

  private List<GitHubRepo> fetch(String pathTemplate, String org) {
    try {
      return github.get()
              .uri(pathTemplate, org)
              .retrieve()
              .onStatus(HttpStatusCode::is4xxClientError, res -> {
                if (res.statusCode().value() == 404) return Mono.empty();
                return res.bodyToMono(String.class)
                        .defaultIfEmpty("GitHub 4xx error")
                        .flatMap(msg -> Mono.error(new ResponseStatusException(res.statusCode(), msg)));
              })
              .onStatus(HttpStatusCode::is5xxServerError, res ->
                      res.bodyToMono(String.class)
                              .defaultIfEmpty("GitHub 5xx error")
                              .flatMap(msg -> Mono.error(new ResponseStatusException(res.statusCode(), msg))))
              .bodyToFlux(GitHubRepo.class)
              .collectList()
              .onErrorResume(ex -> {
                if (ex instanceof ResponseStatusException rse && rse.getStatusCode().value() == 404) {
                  return Mono.empty();
                }
                return Mono.error(ex);
              })
              .block();
    } catch (ResponseStatusException rse) {
      if (rse.getStatusCode().value() == 404) return null;
      throw rse;
    }
  }

  private SortBy normalizeSort(String sort) {
    if (sort == null) return SortBy.STARS;
    String s = sort.toLowerCase(Locale.ROOT);
    return (s.equals("updated")) ? SortBy.UPDATED : SortBy.STARS;
  }

  private int clamp(int val, int min, int max) {
    return Math.max(min, Math.min(max, val));
  }

  private Instant parseInstantSafe(String iso) {
    try {
      return Instant.parse(iso);
    } catch (Exception e) {
      return Instant.EPOCH;
    }
  }
}
