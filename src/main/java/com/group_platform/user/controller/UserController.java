package com.group_platform.user.controller;

import com.group_platform.response.ResponseDto;
import com.group_platform.user.dto.UserDto;
import com.group_platform.user.dto.UserResponseDto;
import com.group_platform.user.entity.User;
import com.group_platform.user.service.UserService;
import com.group_platform.util.UriComponent;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@Controller
@RequestMapping("/user")
@RequiredArgsConstructor
@Validated
public class UserController {

    private final static String USER_DEFAULT_URI = "/users";
    private final UserService userService;

    //회원가입
    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody @Valid UserDto.CreateRequest createRequest) {
        Long id = userService.creatUser(createRequest);
        URI uri = UriComponent.createUri(USER_DEFAULT_URI, id);
        return ResponseEntity.created(uri).build();
    }

    //회원 정보 수정
    @PatchMapping("/{user-id}")
    public ResponseEntity<?> updateUser(@PathVariable("user-id") @Positive Long userId, @RequestBody @Valid UserDto.UpdateRequest updateRequest) {
        updateRequest.setId(userId);
        UserDto.updateResponse updateResponse = userService.updateUser(updateRequest);
        return new ResponseEntity<>(
                new ResponseDto.SingleResponseDto<>(updateResponse), HttpStatus.OK);
    }

    //비밀번호 찾기, 변경
    @PatchMapping("/{user-id}/password")
    public ResponseEntity<?> updatePassword(@PathVariable("user-id") @Positive Long userId, @RequestBody @Valid UserDto.UpdatePasswordRequest updatePasswordRequest) {
        userService.updatePassword(userId,updatePasswordRequest);
        return ResponseEntity.ok().build();
    }

    //회원 정보 조회(자신) - 로그인 방식 구현했을 때 따로 만들것
    //회원 정보 조회(타인)
    @GetMapping("/{user-id}")
    public ResponseEntity<?> getUser(@PathVariable("user-id") @Positive Long userId) {
        UserResponseDto userResponse = userService.getUser(userId);
        return new ResponseEntity<>(
                new ResponseDto.SingleResponseDto<>(userResponse), HttpStatus.OK);
    }

    //회원 탈퇴
    @DeleteMapping("/{user-id}")
    public ResponseEntity<?> deleteUser(@PathVariable("user-id") @Positive Long userId) {
        userService.deleteUser(userId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
    //프로필 사진 업로드 및 수정

    //로그인

    //로그아웃
}
