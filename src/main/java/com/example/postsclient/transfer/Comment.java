package com.example.postsclient.transfer;

// JSON from GET /posts/{id}/comments
public record Comment(Long postId, Long id, String name, String email, String body) {
}
