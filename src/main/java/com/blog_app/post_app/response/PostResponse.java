package com.blog_app.post_app.response;

import com.blog_app.post_app.model.Post;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PostResponse {
    private Long postId;
    private String content;
    private String postedAt;
    private UserResponse author;

    public PostResponse(Post post) {
        this.postId = post.getPostId ();
        this.content = post.getContent ();
        this.postedAt = post.getPostedAt ();
    }
}
