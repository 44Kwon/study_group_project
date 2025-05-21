package com.group_platform.security.controller;

import com.group_platform.security.dto.LoginDto;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Validated
public class AuthController {

    @GetMapping("/api/test")
    public ResponseEntity<?> test(HttpServletRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("Authentication: " + auth);
        return ResponseEntity.ok("OK");
    }

    // 보통 Security를 쓰면 필터를 통해서 처리한다
    // 로그인 같은 경우 customfilter를 통해 처리하고
    // 로그아웃은 내부적으로 설정에 의해 처리된다
//    private final AuthenticationManager authenticationManager;
//
//    public AuthController(AuthenticationManager authenticationManager) {
//        this.authenticationManager = authenticationManager;
//    }
//
//    @PostMapping("/api/login")
//    public ResponseEntity<?> login(@RequestBody LoginDto loginDto) {
//        UsernamePasswordAuthenticationToken authToken =
//                new UsernamePasswordAuthenticationToken(loginDto.getUsername(), loginDto.getPassword());
//
//        Authentication authentication = authenticationManager.authenticate(authToken);
//        SecurityContextHolder.getContext().setAuthentication(authentication);
//
//        return ResponseEntity.ok("로그인 성공");
//    }

    //springconfig에서 logout설정을 하였기 때문에 api만들 필요가 없다
//    @PostMapping("/logout")
//    public ResponseEntity<?> logout() {
//        return ResponseEntity.ok("asd");
//    }
}
