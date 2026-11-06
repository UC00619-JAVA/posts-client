package com.example.postsclient.transfer;

// JSON from GET /posts/{id}
public record Post(Long userId, Long id, String title, String body) {
}
