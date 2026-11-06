package com.example.postsclient.connector;

import com.example.postsclient.transfer.Comment;
import com.example.postsclient.transfer.NewPost;
import com.example.postsclient.transfer.Post;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

// Unit test of the connector: the RestClient talks to a MockRestServiceServer instead of the internet.
class PostClientTest {

    private static final String BASE_URL = "https://jsonplaceholder.test";

    private MockRestServiceServer server;
    private PostClient postClient;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl(BASE_URL);
        server = MockRestServiceServer.bindTo(builder).build();
        postClient = new PostClient(builder.build());
    }

    @Test
    void findByUser_callsGetPostsWithUserId() {
        server.expect(requestTo(BASE_URL + "/posts?userId=1"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        [{"userId":1,"id":1,"title":"t1","body":"b1"},
                         {"userId":1,"id":2,"title":"t2","body":"b2"}]
                        """, MediaType.APPLICATION_JSON));

        List<Post> posts = postClient.findByUser(1L);

        assertThat(posts).containsExactly(new Post(1L, 1L, "t1", "b1"), new Post(1L, 2L, "t2", "b2"));
        server.verify();
    }

    @Test
    void findById_mapsJsonToPost() {
        server.expect(requestTo(BASE_URL + "/posts/1"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        {"userId":1,"id":1,"title":"title","body":"body"}
                        """, MediaType.APPLICATION_JSON));

        assertThat(postClient.findById(1L)).isEqualTo(new Post(1L, 1L, "title", "body"));
        server.verify();
    }

    @Test
    void findById_notFound_throwsNotFound() {
        server.expect(requestTo(BASE_URL + "/posts/999"))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        assertThatThrownBy(() -> postClient.findById(999L))
                .isInstanceOf(HttpClientErrorException.NotFound.class);
    }

    @Test
    void findComments_mapsJsonToComments() {
        server.expect(requestTo(BASE_URL + "/posts/1/comments"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        [{"postId":1,"id":1,"name":"n","email":"a@x.com","body":"c"}]
                        """, MediaType.APPLICATION_JSON));

        assertThat(postClient.findComments(1L))
                .containsExactly(new Comment(1L, 1L, "n", "a@x.com", "c"));
        server.verify();
    }

    @Test
    void create_sendsJsonBodyAndReadsCreatedPost() {
        server.expect(requestTo(BASE_URL + "/posts"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json("""
                        {"userId":1,"title":"My first post","body":"Hello"}
                        """))
                .andRespond(withStatus(HttpStatus.CREATED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("""
                                {"userId":1,"id":101,"title":"My first post","body":"Hello"}
                                """));

        Post created = postClient.create(new NewPost(1L, "My first post", "Hello"));

        assertThat(created).isEqualTo(new Post(1L, 101L, "My first post", "Hello"));
        server.verify();
    }
}
