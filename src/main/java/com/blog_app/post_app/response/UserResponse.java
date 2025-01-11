package com.blog_app.post_app.response;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserResponse {
    private Long userId;
    private String name;
    private String password;
    private String email;




}

