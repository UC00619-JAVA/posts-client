package com.example.postsclient;

import com.github.tomakehurst.wiremock.http.Fault;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.RestClient;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.notFound;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test: the whole application runs on a random port (controller -> service -> connector -> real HTTP).
 * JSONPlaceholder is replaced by a local WireMock server, so the test does not need the internet.
 * Runs with "mvn verify" (maven-failsafe-plugin picks up *IT classes).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PostApiIT {

    @RegisterExtension
    static WireMockExtension jsonPlaceholder = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort())
            .build();

    @DynamicPropertySource
    static void externalApiUrl(DynamicPropertyRegistry registry) {
        registry.add("jsonplaceholder.base-url", jsonPlaceholder::baseUrl);
    }

    @LocalServerPort
    private int port;

    private RestClient api;

    @BeforeEach
    void setUp() {
        // Do not throw on 4xx/5xx: we want to assert on the status ourselves
        api = RestClient.builder()
                .baseUrl("http://localhost:" + port)
                .defaultStatusHandler(status -> true, (request, response) -> { })
                .build();
    }

    @Test
    void findByUser_returnsPostsFromExternalApi() {
        jsonPlaceholder.stubFor(get(urlPathEqualTo("/posts"))
                .withQueryParam("userId", equalTo("1"))
                .willReturn(okJson("""
                        [{"userId":1,"id":1,"title":"t1","body":"b1"},
                         {"userId":1,"id":2,"title":"t2","body":"b2"}]
                        """)));

        ResponseEntity<String> response = api.get().uri("/api/posts?userId=1").retrieve().toEntity(String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("\"id\":1", "\"id\":2", "\"title\":\"t2\"");
    }

    @Test
    void getDetails_combinesPostAndComments() {
        jsonPlaceholder.stubFor(get(urlEqualTo("/posts/1"))
                .willReturn(okJson("""
                        {"userId":1,"id":1,"title":"title","body":"body"}
                        """)));
        jsonPlaceholder.stubFor(get(urlEqualTo("/posts/1/comments"))
                .willReturn(okJson("""
                        [{"postId":1,"id":1,"name":"n1","email":"a@x.com","body":"c1"},
                         {"postId":1,"id":2,"name":"n2","email":"b@x.com","body":"c2"}]
                        """)));

        ResponseEntity<String> response = api.get().uri("/api/posts/1").retrieve().toEntity(String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualToIgnoringWhitespace("""
                {"id":1,"title":"title","body":"body","commentCount":2}
                """);
    }

    @Test
    void getDetails_postNotFound_returns404() {
        jsonPlaceholder.stubFor(get(urlEqualTo("/posts/999")).willReturn(notFound()));

        ResponseEntity<String> response = api.get().uri("/api/posts/999").retrieve().toEntity(String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isEqualToIgnoringWhitespace("""
                {"status":404,"code":"NOT_FOUND","message":"Resource not found in JSONPlaceholder"}
                """);
    }

    @Test
    void getDetails_externalApiDown_returns503() {
        jsonPlaceholder.stubFor(get(urlEqualTo("/posts/1"))
                .willReturn(aResponse().withFault(Fault.CONNECTION_RESET_BY_PEER)));

        ResponseEntity<String> response = api.get().uri("/api/posts/1").retrieve().toEntity(String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(response.getBody()).contains("\"code\":\"EXTERNAL_API_UNAVAILABLE\"");
    }

    @Test
    void create_forwardsPostAndReturns201() {
        jsonPlaceholder.stubFor(post(urlEqualTo("/posts"))
                .willReturn(aResponse()
                        .withStatus(201)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {"userId":1,"id":101,"title":"My first post","body":"Hello"}
                                """)));

        ResponseEntity<String> response = api.post().uri("/api/posts")
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                        {"userId":1,"title":"My first post","body":"Hello"}
                        """)
                .retrieve()
                .toEntity(String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getHeaders().getLocation()).hasToString("/api/posts/101");
        assertThat(response.getBody()).contains("\"id\":101");
        jsonPlaceholder.verify(postRequestedFor(urlEqualTo("/posts"))
                .withRequestBody(equalToJson("""
                        {"userId":1,"title":"My first post","body":"Hello"}
                        """)));
    }

    @Test
    void findCommenters_returnsEmails() {
        jsonPlaceholder.stubFor(get(urlEqualTo("/posts/1/comments"))
                .willReturn(okJson("""
                        [{"postId":1,"id":1,"name":"n1","email":"a@x.com","body":"c1"},
                         {"postId":1,"id":2,"name":"n2","email":"b@x.com","body":"c2"}]
                        """)));

        ResponseEntity<String> response = api.get().uri("/api/posts/1/commenters").retrieve().toEntity(String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("[\"a@x.com\",\"b@x.com\"]");
    }
}
