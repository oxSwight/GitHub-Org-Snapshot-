package com.example.githuborgsnapshot.controller;

import com.example.githuborgsnapshot.dto.RepoDto;
import com.example.githuborgsnapshot.service.GitHubService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "/api/org")
public class OrgController {

  private final GitHubService gitHubService;

  public OrgController(GitHubService gitHubService) {
    this.gitHubService = gitHubService;
  }

  @GetMapping(value = "/{org}/repos", produces = MediaType.APPLICATION_JSON_VALUE)
  public List<RepoDto> topRepos(
          @PathVariable("org") @NotBlank(message = "org must not be blank") String org,
          @RequestParam(name = "sort", defaultValue = "stars") String sort,
          @RequestParam(name = "limit", defaultValue = "5") @Min(1) @Max(20) int limit
  ) {
    return gitHubService.getTopRepos(org.trim(), sort, limit);
  }
}
