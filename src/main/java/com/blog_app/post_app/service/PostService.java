package com.blog_app.post_app.service;

import com.blog_app.post_app.model.Post;
import com.blog_app.post_app.repository.PostRepository;
import com.blog_app.post_app.request.PostRequest;
import com.blog_app.post_app.response.UserResponse;
import org.antlr.v4.runtime.misc.LogManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class PostService {

    private final PostRepository repository;
    private final RestTemplate client;
@Autowired
    public PostService(PostRepository repository, RestTemplate client) {
        this.repository = repository;
    this.client = client;
}

    public void createPost(PostRequest request) {
        Long userId = request.getUserId ( );
        Boolean userExisted=client.getForObject ( "http://localhost:8080/user/isUserExist/{userId}",Boolean.class,userId );
        if (userExisted) {
            Post post=new Post ();
            post.setContent ( request.getGetContent ( ) );
            post.setAuthor ( userId );
            repository.save (post);
        }
        else {
            throw new RuntimeException ( "User not Exist" );
        }
    }
}
