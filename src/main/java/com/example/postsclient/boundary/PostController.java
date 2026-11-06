package com.example.postsclient.boundary;

import com.example.postsclient.transfer.NewPost;
import com.example.postsclient.transfer.Post;
import com.example.postsclient.dto.PostDetails;
import com.example.postsclient.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    // GET /api/posts?userId=1
    @GetMapping
    public List<Post> findByUser(@RequestParam Long userId) {
        return postService.findByUser(userId);
    }

    // GET /api/posts/1
    @GetMapping("/{id}")
    public PostDetails getDetails(@PathVariable Long id) {
        return postService.getDetails(id);
    }

    // POST /api/posts
    @PostMapping
    public ResponseEntity<Post> create(@RequestBody NewPost newPost) {
        Post created = postService.create(newPost);
        return ResponseEntity.created(URI.create("/api/posts/" + created.id())).body(created);
    }

    // Extra: GET /api/posts/1/commenters
    @GetMapping("/{id}/commenters")
    public List<String> findCommenters(@PathVariable Long id) {
        return postService.findCommenters(id);
    }
}
