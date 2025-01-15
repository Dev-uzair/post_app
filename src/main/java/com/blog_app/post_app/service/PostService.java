package com.blog_app.post_app.service;

import com.blog_app.post_app.clients.UserClient;
import com.blog_app.post_app.model.Post;
import com.blog_app.post_app.repository.PostRepository;
import com.blog_app.post_app.request.PostRequest;
import com.blog_app.post_app.response.PostResponse;
import com.blog_app.post_app.response.UserResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class PostService {
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;
    private static final List<String> ALLOWED_EXTENSIONS = List.of ( "jpg", "jpeg", "png", "pdf", "doc", "docx" );
    private final UserClient fiengClientUser;

    private final PostRepository repository;
    private final RestTemplate client;
    private final WebClient webClient;
    @Value("${file.upload.directory}")
    private String uploadDirectory;

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
            postResponse.setAuthor ( fiengClientUser.findIsUserExisted ( userId ) );
            return postResponse;
        } ).toList ( );
    }

    public List<PostResponse> findAll() {

        List<UserResponse> userResponses = fiengClientUser.findAll ( );
        Map<Long, UserResponse> idMappedList =
                userResponses.
                        stream ( )
                        .collect ( Collectors
                                .toMap ( UserResponse::getUserId, Function.identity ( ) )
                        );

        List<PostResponse> list = new ArrayList<> ( );
        for (Post post1 : repository.findAll ( )) {
            PostResponse postResponse = new PostResponse ( post1 );
            postResponse.setAuthor ( idMappedList.getOrDefault ( post1.getAuthor ( ), new UserResponse ( ) ) );
            list.add ( postResponse );
        }

        return list;
    }

    public String uploadPhoto(MultipartFile file) throws IOException {

        validateFile ( file );

        // Create upload directory if it doesn't exist
        Path uploadPath = Paths.get ( uploadDirectory );
        if ( ! Files.exists ( uploadPath ) ) {
            Files.createDirectories ( uploadPath );
        }

        // Generate unique filename
        String originalFilename = StringUtils.cleanPath ( file.getOriginalFilename ( ) );
        String fileExtension = getFileExtension ( originalFilename );
        String newFilename = UUID.randomUUID ( ).toString ( ) + "." + fileExtension;

        // Save file
        Path filePath = uploadPath.resolve ( newFilename );
        Files.copy ( file.getInputStream ( ), filePath, StandardCopyOption.REPLACE_EXISTING );

        return newFilename;
    }

    private String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf(".") == -1) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }

    private void validateFile(MultipartFile file) throws IOException {
        // Check if file is empty
        if ( file.isEmpty ( ) ) {
            throw new IOException ( "File is empty" );
        }

        // Check file size
        if ( file.getSize ( ) > MAX_FILE_SIZE ) {
            throw new IOException ( "File size exceeds maximum limit" );
        }

        // Validate file extension
        String fileExtension = getFileExtension ( file.getOriginalFilename ( ) );
        if ( ! ALLOWED_EXTENSIONS.contains ( fileExtension.toLowerCase ( ) ) ) {
            throw new IOException ( "File type not allowed" );
        }

        // Basic virus/malware check (example)
        if ( isFileNameSuspicious ( file.getOriginalFilename ( ) ) ) {
            throw new IOException ( "File appears to be suspicious" );
        }
    }

    private boolean isFileNameSuspicious(String filename) {
        String lowercaseFilename = filename.toLowerCase();
        return lowercaseFilename.contains("virus") ||
                lowercaseFilename.contains(".exe") ||
                lowercaseFilename.contains(".bat") ||
                lowercaseFilename.contains(".cmd") ||
                lowercaseFilename.contains(".sh");
    }
}

