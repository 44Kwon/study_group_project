package com.group_platform.user.controller;

import com.group_platform.post.dto.PostDto;
import com.group_platform.response.ResponseDto;
import com.group_platform.security.dto.CustomUserDetails;
import com.group_platform.user.dto.UserDto;
import com.group_platform.user.dto.UserMyProfileDto;
import com.group_platform.user.dto.UserResponseDto;
import com.group_platform.user.service.UserService;
import com.group_platform.util.UriComponent;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
//@RequestMapping
@RequiredArgsConstructor
@Validated
@Tag(name = "Users", description = "유저관련")
public class UserController {

    private final static String USER_DEFAULT_URI = "/users";
    private final UserService userService;

    //회원가입
    @Operation(summary = "회원가입", description = "요청을 받아 회원가입을 진행합니다")
    @PostMapping("/join")
    public ResponseEntity<?> createUser(@RequestBody @Valid UserDto.CreateRequest createRequest, HttpServletRequest request) {
        Long id = userService.creatUser(createRequest);
        URI uri = UriComponent.createUri(USER_DEFAULT_URI, id);
        return ResponseEntity.created(uri).build();
    }

    //회원 정보 수정
    @PatchMapping("/users/my")
    @Operation(summary = "회원정보 수정",description = "요청을 받아 회원정보를 수정합니다")
    public ResponseEntity<?> updateUser(@AuthenticationPrincipal CustomUserDetails userDetails, @RequestBody @Valid UserDto.UpdateRequest updateRequest) {
        UserDto.UpdateResponse updateResponse = userService.updateUser(userDetails.getId(),updateRequest);
        return new ResponseEntity<>(
                new ResponseDto.SingleResponseDto<>(updateResponse), HttpStatus.OK);
    }

    //비밀번호 찾기, 변경
    @PatchMapping("/users/my/password")
    @Operation(summary = "비밀번호 변경", description = "회원정보 수정과는 다르게 비밀번호 변경은 따로 요청합니다")
    public ResponseEntity<?> updatePassword(@AuthenticationPrincipal CustomUserDetails userDetails, @RequestBody @Valid UserDto.UpdatePasswordRequest updatePasswordRequest) {
        userService.updatePassword(userDetails.getId(),updatePasswordRequest);
        return ResponseEntity.ok().build();
    }

    //회원 정보 조회(자신) - 나중에 타인 것과 다르게 설정을 할 것
    @GetMapping("/users/my")
    @Operation(summary = "나의 정보 조회")
    public ResponseEntity<?> getMyInfo(@AuthenticationPrincipal CustomUserDetails userDetails) {
        UserMyProfileDto myUserResponse = userService.getMyUser(userDetails.getId());
        return new ResponseEntity<>(
                new ResponseDto.SingleResponseDto<>(myUserResponse), HttpStatus.OK);
    }

    //회원 정보 조회시 찜 페이지네이션
    @GetMapping("/users/my/bookmarks")
    public ResponseEntity<?> getMyBookmarks(@AuthenticationPrincipal CustomUserDetails userDetails,
                                            @PageableDefault(size = 10) Pageable pageable) {
        Page<PostDto.PostBookMarkResponse> myPostBookmarks = userService.getMyPostBookmarks(userDetails.getId(), pageable);
        return ResponseEntity.ok(new ResponseDto.MultipleResponseDto<>(myPostBookmarks.getContent(), myPostBookmarks));
    }


    //회원 정보 조회(타인)
    @GetMapping("/users/{user-id}")
    @Operation(summary = "타인의 정보를 조회")
    public ResponseEntity<?> getOtherUserInfo(@PathVariable("user-id") @Positive Long userId) {
        UserResponseDto userResponse = userService.getUser(userId);
        return new ResponseEntity<>(
                new ResponseDto.SingleResponseDto<>(userResponse), HttpStatus.OK);
    }

    //회원 탈퇴
    @DeleteMapping("/users/my")
    @Operation(summary = "회원탈퇴를 진행합니다")
    public ResponseEntity<?> deleteUser(@AuthenticationPrincipal CustomUserDetails userDetails, HttpServletRequest request) {
        userService.deleteUser(userDetails.getId());

        //세션 무효화
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        SecurityContextHolder.clearContext();
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
    //프로필 사진 업로드 및 수정
}
