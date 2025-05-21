package com.group_platform.user.controller;

import com.group_platform.response.ResponseDto;
import com.group_platform.security.dto.CustomUserDetails;
import com.group_platform.user.dto.UserDto;
import com.group_platform.user.dto.UserResponseDto;
import com.group_platform.user.entity.User;
import com.group_platform.user.service.UserService;
import com.group_platform.util.UriComponent;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
//@RequestMapping
@RequiredArgsConstructor
@Validated
public class UserController {

    private final static String USER_DEFAULT_URI = "/users";
    private final UserService userService;

    //회원가입
    @PostMapping("/join")
    public ResponseEntity<?> createUser(@RequestBody @Valid UserDto.CreateRequest createRequest, HttpServletRequest request) {
        Long id = userService.creatUser(createRequest);
        URI uri = UriComponent.createUri(USER_DEFAULT_URI, id);
        return ResponseEntity.created(uri).build();
    }

    //회원 정보 수정
    @PatchMapping("/users/my")
    public ResponseEntity<?> updateUser(@AuthenticationPrincipal CustomUserDetails userDetails, @RequestBody @Valid UserDto.UpdateRequest updateRequest) {
        UserDto.updateResponse updateResponse = userService.updateUser(userDetails.getId(),updateRequest);
        return new ResponseEntity<>(
                new ResponseDto.SingleResponseDto<>(updateResponse), HttpStatus.OK);
    }

    //비밀번호 찾기, 변경
    @PatchMapping("/users/my/password")
    public ResponseEntity<?> updatePassword(@AuthenticationPrincipal CustomUserDetails userDetails, @RequestBody @Valid UserDto.UpdatePasswordRequest updatePasswordRequest) {
        userService.updatePassword(userDetails.getId(),updatePasswordRequest);
        return ResponseEntity.ok().build();
    }

    //회원 정보 조회(자신) - 로그인 방식 구현했을 때 따로 만들것

    //회원 정보 조회(타인)
    //최소 정보만 수정하기
    @GetMapping("/users/{user-id}")
    public ResponseEntity<?> getUser(@PathVariable("user-id") @Positive Long userId) {
        UserResponseDto userResponse = userService.getUser(userId);
        return new ResponseEntity<>(
                new ResponseDto.SingleResponseDto<>(userResponse), HttpStatus.OK);
    }

    //회원 탈퇴
    @DeleteMapping("/users/my")
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

    //로그인

    //로그아웃
}
