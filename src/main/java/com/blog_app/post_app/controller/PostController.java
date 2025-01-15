package com.blog_app.post_app.controller;

import com.blog_app.post_app.request.PostRequest;
import com.blog_app.post_app.response.PostResponse;
import com.blog_app.post_app.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/post")
public class PostController {
    private final PostService service;

    @Autowired
    public PostController(PostService service) {
        this.service = service;
    }

    @PostMapping("/")
    public void createPost(@RequestBody PostRequest request) {
        service.createPost ( request );
    }


    @PostMapping("/upload")
    public String uploadPhoto(@RequestParam("file")MultipartFile photo){
        try {
            return service.uploadPhoto(photo);
        }
        catch ( IOException e ) {
            throw new RuntimeException ( e );
        }
    }
    @GetMapping("/postByUserId/{userId}")
    public List<PostResponse> findAllPostByUserId(@PathVariable Long userId) {
        return service.findByUserId ( userId );
    }
    @GetMapping("/")
    public List<PostResponse> findAll(){
        return service.findAll();
    }

}
