package com.example.githuborgsnapshot.service;

import com.example.githuborgsnapshot.dto.RepoDto;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.*;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

class GitHubServiceTest {

    WireMockServer wm;
    GitHubService service;

    @BeforeEach
    void setup() {
        wm = new WireMockServer(0);
        wm.start();
        configureFor("localhost", wm.port());

        WebClient client = WebClient.builder()
                .baseUrl("http://localhost:" + wm.port())
                .defaultHeader(HttpHeaders.USER_AGENT, "test-agent")
                .build();

        service = new GitHubService(client);
    }

    @AfterEach
    void tearDown() {
        wm.stop();
    }

    @Test
    void topByStars_sorted_and_limited() {
        stubFor(get(urlPathEqualTo("/orgs/vercel/repos"))
                .withQueryParam("per_page", equalTo("100"))
                .willReturn(okJson("""
            [
              {"name":"a","html_url":"u1","stargazers_count":5,"forks_count":1,"language":"JS","updated_at":"2024-01-01T00:00:00Z","description":"a"},
              {"name":"b","html_url":"u2","stargazers_count":10,"forks_count":2,"language":"TS","updated_at":"2024-01-02T00:00:00Z","description":"b"},
              {"name":"c","html_url":"u3","stargazers_count":7,"forks_count":3,"language":"Go","updated_at":"2024-01-03T00:00:00Z","description":"c"}
            ]
            """)));

        List<RepoDto> res = service.getTopRepos("vercel", "stars", 2);
        assertEquals(2, res.size());
        assertEquals("b", res.get(0).name());
        assertEquals("c", res.get(1).name());
    }

    @Test
    void topByUpdated_sorted_desc() {
        stubFor(get(urlPathEqualTo("/orgs/acme/repos"))
                .withQueryParam("per_page", equalTo("100"))
                .willReturn(okJson("""
            [
              {"name":"x","html_url":"u1","stargazers_count":1,"forks_count":0,"language":null,"updated_at":"2024-01-03T00:00:00Z","description":null},
              {"name":"y","html_url":"u2","stargazers_count":2,"forks_count":0,"language":null,"updated_at":"2024-01-05T00:00:00Z","description":null},
              {"name":"z","html_url":"u3","stargazers_count":3,"forks_count":0,"language":null,"updated_at":"2024-01-04T00:00:00Z","description":null}
            ]
            """)));

        List<RepoDto> res = service.getTopRepos("acme", "updated", 3);
        assertEquals(List.of("y","z","x"),
                res.stream().map(RepoDto::name).toList());
    }

    @Test
    void fallback_to_users_when_org_404() {
        stubFor(get(urlPathEqualTo("/orgs/userlike/repos"))
                .withQueryParam("per_page", equalTo("100"))
                .willReturn(aResponse().withStatus(404)));

        stubFor(get(urlPathEqualTo("/users/userlike/repos"))
                .withQueryParam("per_page", equalTo("100"))
                .willReturn(okJson("""
            [
              {"name":"only","html_url":"u","stargazers_count":0,"forks_count":0,"language":"Kotlin","updated_at":"2023-12-31T00:00:00Z","description":"d"}
            ]
            """)));

        List<RepoDto> res = service.getTopRepos("userlike", "stars", 5);
        assertEquals(1, res.size());
        assertEquals("only", res.get(0).name());
    }

    @Test
    void not_found_when_both_404() {
        stubFor(get(urlPathEqualTo("/orgs/none/repos"))
                .withQueryParam("per_page", equalTo("100"))
                .willReturn(aResponse().withStatus(404)));
        stubFor(get(urlPathEqualTo("/users/none/repos"))
                .withQueryParam("per_page", equalTo("100"))
                .willReturn(aResponse().withStatus(404)));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.getTopRepos("none", "stars", 5));
        assertEquals(404, ex.getStatusCode().value());
    }

    @Test
    void limit_is_clamped_1_20() {
        stubFor(get(urlPathEqualTo("/orgs/lim/repos"))
                .withQueryParam("per_page", equalTo("100"))
                .willReturn(okJson("""
            [
              {"name":"a","html_url":"u1","stargazers_count":1,"forks_count":0,"language":"x","updated_at":"2024-01-01T00:00:00Z","description":null},
              {"name":"b","html_url":"u2","stargazers_count":2,"forks_count":0,"language":"x","updated_at":"2024-01-02T00:00:00Z","description":null},
              {"name":"c","html_url":"u3","stargazers_count":3,"forks_count":0,"language":"x","updated_at":"2024-01-03T00:00:00Z","description":null}
            ]
            """)));

        assertEquals(1, service.getTopRepos("lim", "stars", 0).size());
        assertEquals(3, service.getTopRepos("lim", "stars", 50).size());
    }
}
