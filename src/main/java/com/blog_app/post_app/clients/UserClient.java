package com.blog_app.post_app.clients;

import com.blog_app.post_app.response.UserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "USER-APP")
public interface UserClient {
    @GetMapping("/user/findById/{userId}")
    public UserResponse findById(@PathVariable Long userId);

    @GetMapping("user/")
    public List<UserResponse> findAll();
}
