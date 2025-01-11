package com.blog_app.post_app.service;

import com.blog_app.post_app.clients.UserClient;
import com.blog_app.post_app.model.Post;
import com.blog_app.post_app.repository.PostRepository;
import com.blog_app.post_app.request.PostRequest;
import com.blog_app.post_app.response.PostResponse;
import com.blog_app.post_app.response.UserResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class PostService {
    private final UserClient fiengClientUser;

    private final PostRepository repository;
    private final RestTemplate client;
    private final WebClient webClient;

    @Autowired
    public PostService(UserClient fiengClientUser, PostRepository repository, RestTemplate client, WebClient webClient) {
        this.fiengClientUser = fiengClientUser;
        this.repository = repository;
        this.client = client;
        this.webClient = webClient;
    }

    public void createPost(PostRequest request) {
        Long userId = request.getUserId ( );

//        Boolean withRestClient = client.getForObject ( "http://localhost:8080/user/isUserExist/{userId}", Boolean.class, userId );

        Boolean withWebClient = webClient.get ( )
                .uri ( "http://localhost:8080/user/isUserExist/" + userId )
                .retrieve ( )
                .bodyToMono ( Boolean.class )
                .block ( );
        if ( Boolean.TRUE.equals ( withWebClient ) ) {
            Post post = new Post ( );
            post.setContent ( request.getGetContent ( ) );
            post.setAuthor ( userId );
            repository.save ( post );
        } else {
            throw new RuntimeException ( "User not Exist" );
        }
    }


//        UserResponse withWebClient = webClient.get ( )
//                .uri ( "http://localhost:8080/user/findById/" + userId )
//                .retrieve ( )
//                .bodyToMono ( UserResponse.class )
//                .block ( )
//                ;
//
//        Mono<UserResponse> nonBlockingWebClient = webClient.get ( )
//                .uri ( "http://localhost:8080/user/findById/" + userId )
//                .retrieve ( )
//                .bodyToMono ( UserResponse.class )
//                 ;
    public List<PostResponse> findByUserId(Long userId) {
        List<Post> postsByAuthor = repository.findPostsByAuthor ( userId );
        return postsByAuthor.stream ( ).map ( post -> {
            PostResponse postResponse = new PostResponse ( post );
            postResponse.setAuthor ( fiengClientUser.findById ( userId ) );
            return postResponse;
        } ).toList ( );
    }

    public List<PostResponse> findAll() {

        List<UserResponse> userResponses = fiengClientUser.findAll ( );
        Map<Long, UserResponse>idMappedList =
                userResponses.
                        stream ( )
                        .collect ( Collectors
                                .toMap ( UserResponse::getUserId,Function.identity ())
                                        );

        List<PostResponse> list = new ArrayList<> ( );
        for (Post post1 : repository.findAll ( )) {
            PostResponse postResponse = new PostResponse ( post1 );
            postResponse.setAuthor ( idMappedList.getOrDefault ( post1.getAuthor (),new UserResponse () ) );
            list.add ( postResponse );
        }

        return list;
    }
}
