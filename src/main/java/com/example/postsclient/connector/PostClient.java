package com.example.postsclient.connector;

import com.example.postsclient.transfer.Comment;
import com.example.postsclient.transfer.NewPost;
import com.example.postsclient.transfer.Post;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
@RequiredArgsConstructor
public class PostClient {

    private final RestClient restClient;

    // GET /posts?userId=1
    public List<Post> findByUser(Long userId) {
        return restClient.get()
                .uri("/posts?userId={userId}", userId)
                .retrieve()
                .body(new ParameterizedTypeReference<List<Post>>() {});
    }

    // GET /posts/1
    public Post findById(Long id) {
        return restClient.get()
                .uri("/posts/{id}", id)
                .retrieve()
                .body(Post.class);
    }

    // GET /posts/1/comments
    public List<Comment> findComments(Long postId) {
        return restClient.get()
                .uri("/posts/{id}/comments", postId)
                .retrieve()
                .body(new ParameterizedTypeReference<List<Comment>>() {});
    }

    // POST /posts  (JSONPlaceholder answers 201 with id 101, but does not save it)
    public Post create(NewPost newPost) {
        return restClient.post()
                .uri("/posts")
                .contentType(MediaType.APPLICATION_JSON)
                .body(newPost)
                .retrieve()
                .body(Post.class);
    }
}
