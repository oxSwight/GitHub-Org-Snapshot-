package com.example.githuborgsnapshot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication
public class GithubOrgSnapshotApplication {
  public static void main(String[] args) {
    SpringApplication.run(GithubOrgSnapshotApplication.class, args);
  }
}
