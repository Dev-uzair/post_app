package com.blog_app.post_app.controller;

import com.blog_app.post_app.request.PostRequest;
import com.blog_app.post_app.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
public class PostController {
    private final PostService service;
@Autowired
    public PostController(PostService service) {
        this.service = service;
    }
@PostMapping("/")
    public void createPost(@RequestBody PostRequest request){
        service.createPost(request);
    }
}
