package com.example.postsclient.service;

import com.example.postsclient.transfer.Comment;
import com.example.postsclient.transfer.NewPost;
import com.example.postsclient.transfer.Post;
import com.example.postsclient.connector.PostClient;
import com.example.postsclient.dto.PostDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostClient postClient;

    public List<Post> findByUser(Long userId) {
        return postClient.findByUser(userId);
    }

    public PostDetails getDetails(Long id) {
        Post post = postClient.findById(id);
        List<Comment> comments = postClient.findComments(id);
        return new PostDetails(post.id(), post.title(), post.body(), comments.size());
    }

    public Post create(NewPost newPost) {
        return postClient.create(newPost);
    }

    // Extra: emails of the people who commented a post
    public List<String> findCommenters(Long postId) {
        List<String> emails = new ArrayList<>();
        for (Comment comment : postClient.findComments(postId)) {
            emails.add(comment.email());
        }
        return emails;
    }
}
