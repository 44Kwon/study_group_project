package com.group_platform.post.controller;

import com.group_platform.post.dto.PostDto;
import com.group_platform.post.service.PostService;
import com.group_platform.response.ResponseDto;
import com.group_platform.security.dto.CustomUserDetails;
import com.group_platform.util.UriComponent;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequiredArgsConstructor
@Validated
public class PostController {

    private final static String POST_DEFAULT_URI = "/posts";
    private final static String GROUP_POST_DEFAULT_URI = "/study-group/{study-group-id}/posts";
    private final PostService postService;

    @PostMapping("/posts")
    //공통게시글 작성
    public ResponseEntity<?> createCommonPost(@AuthenticationPrincipal CustomUserDetails userDetails,
                                              @RequestBody @Valid PostDto.CreateRequestForCommonPost createPost) {
        Long id = postService.crateCommonPost(userDetails.getId(), createPost);
        URI uri = UriComponent.createUri(POST_DEFAULT_URI, id);
        return ResponseEntity.created(uri).build();
    }

    @PostMapping("/study-group/{study-group-id}/posts")
    //그룹내게시글 작성
    public ResponseEntity<?> createGroupPost(@AuthenticationPrincipal CustomUserDetails userDetails,
                                             @RequestBody @Valid PostDto.CreateRequestForGroupPost createPost,
                                             @PathVariable("study-group-id") Long studyGroupId) {
        Long id = postService.createGroupPost(userDetails.getId(), createPost, studyGroupId);
        URI uri = UriComponent.createUri(GROUP_POST_DEFAULT_URI, studyGroupId, id);
        return ResponseEntity.created(uri).build();
    }

    @PatchMapping("/posts/{post-id}")
    public ResponseEntity<?> updatePost(@AuthenticationPrincipal CustomUserDetails userDetails,
                                        @RequestBody @Valid PostDto.UpdateRequest updateRequest,
                                        @PathVariable("post-id") @Positive Long postId) {
        updateRequest.setId(postId);
        PostDto.Response response = postService.updatePost(userDetails.getId(), updateRequest);
        return ResponseEntity.ok(new ResponseDto.SingleResponseDto<>(response));
    }

    @GetMapping("/posts/{post-id}")
    public ResponseEntity<?> getPost(@PathVariable("post-id") @Positive Long postId) {
        PostDto.Response post = postService.getPost(postId);
        return ResponseEntity.ok(new ResponseDto.SingleResponseDto<>(post));
    }

    @DeleteMapping("/posts/{post-id}")
    public ResponseEntity<?> deletePost(@AuthenticationPrincipal CustomUserDetails userDetails,
                                              @PathVariable("post-id") @Positive Long postId) {
        postService.deletePost(userDetails.getId(),postId);
        return ResponseEntity.noContent().build();
    }

//    @GetMapping("/posts")
//    //공통 게시글 조회
//    //(검색/필터 지원 가능)
//    public ResponseEntity<?> getAllCommonPosts() {
//        // (공통 게시글 상단고정 불가 로직 구현 필요)
//    }
//
//    @GetMapping("/posts/search")
//    public ResponseEntity<?> searchCommonPost(@RequestParam String query) {
//
//    }
//
//    //그룹 게시글 목록
//    //(검색/필터 지원 가능)
//    @GetMapping("/study-group/{study-group-id}/posts")
//    public ResponseEntity<?> getAllGroupPosts(@AuthenticationPrincipal CustomUserDetails userDetails,
//                                              @PathVariable("study-group-id") @Positive Long studyGroupId) {
//
//    }
//
//    @GetMapping("/study-group/{study-group-id}/posts/search")
//    public ResponseEntity<?> getAllGroupPosts(@AuthenticationPrincipal CustomUserDetails userDetails,
//                                              @PathVariable("study-group-id") @Positive Long studyGroupId,
//                                              @RequestParam String query) {
//
//    }
    // 공통 게시판에서 당일 상위 10개에 대한 목록(추천개념)


//    POST /posts/{id}/likes : 게시글 좋아요
//
//    DELETE /posts/{id}/likes : 게시글 좋아요 취소
//
//    찜/ 찜취소
    //공통 게시글에 대해서 상단고정은 불가하고, 상단에 당일에 인기글 목록(10개)을 보여준다

    //상단고정, 취소(그룹 내 게시판에서만)
}
