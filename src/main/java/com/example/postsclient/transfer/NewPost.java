package com.example.postsclient.transfer;

// JSON we SEND in POST /posts (no id: the server chooses it)
public record NewPost(Long userId, String title, String body) {
}
