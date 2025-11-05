package com.example.githuborgsnapshot.service;

import com.example.githuborgsnapshot.dto.GitHubRepo;
import com.example.githuborgsnapshot.dto.RepoDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;

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

  public List<GitHubRepo> getRepos(String org) {
    List<GitHubRepo> fromOrg = fetchOrNull("/orgs/{org}/repos?per_page=100", org);
    if (fromOrg != null) return fromOrg;

    List<GitHubRepo> fromUser = fetchOrNull("/users/{org}/repos?per_page=100", org);
    if (fromUser != null) return fromUser;

    throw new ResponseStatusException(HttpStatus.NOT_FOUND,
            "Organization or user '" + org + "' not found on GitHub.");
  }

  private List<GitHubRepo> fetchOrNull(String pathTemplate, String org) {
    try {
      return github.get()
              .uri(pathTemplate, org)
              .exchangeToFlux(resp -> handle(resp))
              .collectList()
              .block();
    } catch (NotFoundMarker nf) {
      return null; // почему: сигнал для fallback
    }
  }

  private Flux<GitHubRepo> handle(ClientResponse resp) {
    HttpStatusCode sc = resp.statusCode();
    int code = sc.value();

    if (code == 404) {
      throw new NotFoundMarker();
    }
    if (code >= 400 && code < 600) {
      return resp.bodyToMono(String.class)
              .defaultIfEmpty(sc.toString())
              .flatMapMany(msg -> Flux.error(new ResponseStatusException(sc, msg)));
    }
    return resp.bodyToFlux(GitHubRepo.class);
  }

  private static class NotFoundMarker extends RuntimeException { }

  private SortBy normalizeSort(String sort) {
    if (sort == null) return SortBy.STARS;
    String s = sort.toLowerCase(Locale.ROOT);
    return (s.equals("updated")) ? SortBy.UPDATED : SortBy.STARS;
  }

  private int clamp(int val, int min, int max) {
    return Math.max(min, Math.min(max, val));
  }

  private Instant parseInstantSafe(String iso) {
    try { return Instant.parse(iso); } catch (Exception e) { return Instant.EPOCH; }
  }
}
