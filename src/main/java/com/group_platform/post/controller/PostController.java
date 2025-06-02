package com.group_platform.post.controller;

import com.group_platform.post.dto.*;
import com.group_platform.post.entity.PostType;
import com.group_platform.post.service.PostService;
import com.group_platform.response.ResponseDto;
import com.group_platform.security.dto.CustomUserDetails;
import com.group_platform.util.UriComponent;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

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
        PostResponseDto response = postService.updatePost(userDetails.getId(), updateRequest);
        return ResponseEntity.ok(new ResponseDto.SingleResponseDto<>(response));
    }

    @GetMapping("/posts/{post-id}")
    public ResponseEntity<?> getCommonPost(@AuthenticationPrincipal CustomUserDetails userDetails,
                                           @PathVariable("post-id") @Positive Long postId) {
        Long userId = userDetails != null ? userDetails.getId() : null;
        PostResponseDto post = postService.getCommonPost(userId,postId);
        return ResponseEntity.ok(new ResponseDto.SingleResponseDto<>(post));
    }

    @GetMapping("/study-group/{study-group-id}/posts/{post-id}")
    public ResponseEntity<?> getGroupPost(@AuthenticationPrincipal CustomUserDetails userDetails,
                                          @PathVariable("study-group-id") @Positive Long groupId,
                                          @PathVariable("post-id") @Positive Long postId) {
        PostResponseDto post = postService.getGroupPost(userDetails.getId(), groupId, postId);
        return ResponseEntity.ok(new ResponseDto.SingleResponseDto<>(post));
    }

    @DeleteMapping("/posts/{post-id}")
    public ResponseEntity<?> deletePost(@AuthenticationPrincipal CustomUserDetails userDetails,
                                              @PathVariable("post-id") @Positive Long postId) {
        postService.deletePost(userDetails.getId(),postId);
        return ResponseEntity.noContent().build();
    }

    //최신순, 댓글 갯수순, 좋아요 순, 조회순
    //게시글 타입별 필터링
    @GetMapping("/posts")
    public ResponseEntity<?> getPagedCommonPosts(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                 @RequestParam(required = false) PostType postType,
                                                 @RequestParam(defaultValue = "LATEST") PostSortType sort,
                                                 @PageableDefault(size = 10) Pageable pageable) {
        Long userId = userDetails != null ? userDetails.getId() : null;
        // (공통 게시글 상단고정 불가)
        Page<PostResponseListDto> commonPosts = postService.getCommonPosts(userId, postType, sort, pageable);
        List<PostResponseListDto> content = commonPosts.getContent();
        return new ResponseEntity<>(new ResponseDto.MultipleResponseDto<>(content,commonPosts), HttpStatus.OK);
    }

    @GetMapping("/study-group/{study-group-id}/posts")
    public ResponseEntity<?> getPagedGroupPosts(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                @PathVariable("study-group-id") @Positive Long groupId,
                                                @RequestParam(required = false) PostType postType,
                                                @RequestParam(defaultValue = "LATEST") PostSortType sort,
                                                @PageableDefault(size = 10) Pageable pageable) {
        //새로운 ResponseDto (고정글 + 게시글목록)만들어서 내보내는중
        PostListResponse groupPosts = postService.getGroupPosts(userDetails.getId(), groupId, postType, sort, pageable);
        return ResponseEntity.ok(new ResponseDto.SingleResponseDto<>(groupPosts));
    }

    @GetMapping("/posts/search")
    public ResponseEntity<?> getPagedSearchCommonPosts(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                       @RequestParam("q") String query,
                                                       @PageableDefault(size = 10) Pageable pageable) {
        Long userId = userDetails != null ? userDetails.getId() : null;
        Page<PostResponseListDto> postResponseListDtos = postService.searchCommonPosts(userId, query, pageable);
        List<PostResponseListDto> content = postResponseListDtos.getContent();
        return new ResponseEntity<>(new ResponseDto.MultipleResponseDto<>(content,postResponseListDtos), HttpStatus.OK);
    }

    @GetMapping("/study-group/{study-group-id}/posts/search")
    public ResponseEntity<?> getPagedSearchGroupPosts(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                      @PathVariable("study-group-id") @Positive Long groupId,
                                                      @RequestParam("q") String query,
                                                      @PageableDefault(size = 10) Pageable pageable) {
        PostListResponse postListResponse = postService.searchGroupPosts(userDetails.getId(), groupId, query, pageable);
        return ResponseEntity.ok(new ResponseDto.SingleResponseDto<>(postListResponse));
    }

    @PatchMapping("/study-group/{study-group-id}/posts/{post-id}/fixed")
    //상단고정(리더만가능)
    public ResponseEntity<?> pinPost(@AuthenticationPrincipal CustomUserDetails userDetails,
                                     @PathVariable("study-group-id") @Positive Long groupId,
                                     @PathVariable("post-id") @Positive Long postId) {
        PostDto.UpdatePinnedResponse updatePinnedResponse = postService.updatePinned(userDetails.getId(), groupId, postId);
        return new ResponseEntity<>(new ResponseDto.SingleResponseDto<>(updatePinnedResponse), HttpStatus.OK);
    }

    @PatchMapping("/study-group/{study-group-id}/posts/{post-id}/unfixed")
    //상단고정취소(리더만가능)
    public ResponseEntity<?> unpinPost(@AuthenticationPrincipal CustomUserDetails userDetails,
                                       @PathVariable("study-group-id") @Positive Long groupId,
                                       @PathVariable("post-id") @Positive Long postId) {
        PostDto.UpdatePinnedResponse updatePinnedResponse = postService.updateUnPinned(userDetails.getId(), groupId, postId);
        return new ResponseEntity<>(new ResponseDto.SingleResponseDto<>(updatePinnedResponse), HttpStatus.OK);
    }

    //좋아요
    @PostMapping("/posts/{post-id}/likes")
    public ResponseEntity<?> likePost(@AuthenticationPrincipal CustomUserDetails userDetails,
                                      @PathVariable("post-id") @Positive Long postId) {
        return ResponseEntity.ok(new ResponseDto.SingleResponseDto<>(postService.likePost(userDetails.getId(), postId)));
    }

    //좋아요취소
    @DeleteMapping("/posts/{post-id}/likes")
    public ResponseEntity<?> cancelLikePost(@AuthenticationPrincipal CustomUserDetails userDetails,
                                            @PathVariable("post-id") @Positive Long postId) {
        return ResponseEntity.ok(new ResponseDto.SingleResponseDto<>(postService.cancelLikePost(userDetails.getId(), postId)));
    }

    //찜
    @PostMapping("/posts/{post-id}/favorites")
    public ResponseEntity<?> favoritePost(@AuthenticationPrincipal CustomUserDetails userDetails,
                                          @PathVariable("post-id") @Positive Long postId) {
        return ResponseEntity.ok(new ResponseDto.SingleResponseDto<>(postService.favoritePost(userDetails.getId(), postId)));
    }

    //찜 취소
    @DeleteMapping("/posts/{post-id}/favorites")
    public ResponseEntity<?> cancelFavoritePost(@AuthenticationPrincipal CustomUserDetails userDetails,
                                          @PathVariable("post-id") @Positive Long postId) {
        return ResponseEntity.ok(new ResponseDto.SingleResponseDto<>(postService.cancelFavoritePost(userDetails.getId(), postId)));
    }
//    @GetMapping("/posts/top")
//    // 공통 게시판에서 당일 상위 10개에 대한 목록(추천개념)
//    public ResponseEntity<?> getTopCommonPosts() {
//        return null;
//    }
}
