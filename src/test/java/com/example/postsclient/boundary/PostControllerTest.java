package com.example.postsclient.boundary;

import com.example.postsclient.dto.PostDetails;
import com.example.postsclient.service.PostService;
import com.example.postsclient.transfer.NewPost;
import com.example.postsclient.transfer.Post;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Web layer slice: only the controller + GlobalExceptionHandler are loaded; the service is a mock.
@WebMvcTest(PostController.class)
class PostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PostService postService;

    @Test
    void findByUser_returns200WithPosts() throws Exception {
        when(postService.findByUser(1L)).thenReturn(List.of(
                new Post(1L, 1L, "t1", "b1"), new Post(1L, 2L, "t2", "b2")));

        mockMvc.perform(get("/api/posts").param("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].title").value("t2"));
    }

    @Test
    void findByUser_withoutUserId_returns400() throws Exception {
        mockMvc.perform(get("/api/posts"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getDetails_returns200WithDetails() throws Exception {
        when(postService.getDetails(1L)).thenReturn(new PostDetails(1L, "title", "body", 5));

        mockMvc.perform(get("/api/posts/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("title"))
                .andExpect(jsonPath("$.commentCount").value(5));
    }

    @Test
    void getDetails_externalNotFound_returns404WithApiError() throws Exception {
        when(postService.getDetails(999L)).thenThrow(mock(HttpClientErrorException.NotFound.class));

        mockMvc.perform(get("/api/posts/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    @Test
    void getDetails_externalUnreachable_returns503WithApiError() throws Exception {
        when(postService.getDetails(1L)).thenThrow(new ResourceAccessException("Connection refused"));

        mockMvc.perform(get("/api/posts/1"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value(503))
                .andExpect(jsonPath("$.code").value("EXTERNAL_API_UNAVAILABLE"));
    }

    @Test
    void create_returns201WithLocation() throws Exception {
        when(postService.create(any(NewPost.class)))
                .thenReturn(new Post(1L, 101L, "My first post", "Hello"));

        mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"userId":1,"title":"My first post","body":"Hello"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/posts/101"))
                .andExpect(jsonPath("$.id").value(101))
                .andExpect(jsonPath("$.title").value("My first post"));
    }

    @Test
    void findCommenters_returns200WithEmails() throws Exception {
        when(postService.findCommenters(1L)).thenReturn(List.of("a@x.com", "b@x.com"));

        mockMvc.perform(get("/api/posts/1/commenters"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0]").value("a@x.com"));
    }
}
