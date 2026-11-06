package com.example.postsclient.service;

import com.example.postsclient.connector.PostClient;
import com.example.postsclient.dto.PostDetails;
import com.example.postsclient.transfer.Comment;
import com.example.postsclient.transfer.NewPost;
import com.example.postsclient.transfer.Post;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Unit test: no Spring, no HTTP. The PostClient is a Mockito mock.
@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostClient postClient;

    @InjectMocks
    private PostService postService;

    @Test
    void findByUser_returnsPostsFromClient() {
        List<Post> posts = List.of(new Post(1L, 1L, "t1", "b1"), new Post(1L, 2L, "t2", "b2"));
        when(postClient.findByUser(1L)).thenReturn(posts);

        assertThat(postService.findByUser(1L)).isEqualTo(posts);
    }

    @Test
    void getDetails_combinesPostAndCommentCount() {
        when(postClient.findById(1L)).thenReturn(new Post(1L, 1L, "title", "body"));
        when(postClient.findComments(1L)).thenReturn(List.of(
                new Comment(1L, 1L, "n1", "a@x.com", "c1"),
                new Comment(1L, 2L, "n2", "b@x.com", "c2")));

        PostDetails details = postService.getDetails(1L);

        assertThat(details).isEqualTo(new PostDetails(1L, "title", "body", 2));
    }

    @Test
    void getDetails_withoutComments_hasZeroCount() {
        when(postClient.findById(5L)).thenReturn(new Post(1L, 5L, "title", "body"));
        when(postClient.findComments(5L)).thenReturn(List.of());

        assertThat(postService.getDetails(5L).commentCount()).isZero();
    }

    @Test
    void create_delegatesToClient() {
        NewPost newPost = new NewPost(1L, "My first post", "Hello");
        Post created = new Post(1L, 101L, "My first post", "Hello");
        when(postClient.create(newPost)).thenReturn(created);

        assertThat(postService.create(newPost)).isEqualTo(created);
        verify(postClient).create(newPost);
    }

    @Test
    void findCommenters_returnsEmailsInOrder() {
        when(postClient.findComments(1L)).thenReturn(List.of(
                new Comment(1L, 1L, "n1", "a@x.com", "c1"),
                new Comment(1L, 2L, "n2", "b@x.com", "c2")));

        assertThat(postService.findCommenters(1L)).containsExactly("a@x.com", "b@x.com");
    }
}
