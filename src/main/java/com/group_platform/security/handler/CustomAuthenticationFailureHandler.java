package com.group_platform.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {

    private final ObjectMapper objectMapper;

    public CustomAuthenticationFailureHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");

        //에러별 분기처리
        String message = switch (exception) {
            case AuthenticationServiceException authenticationServiceException -> exception.getMessage();
            case BadCredentialsException badCredentialsException -> "아이디 또는 비밀번호가 올바르지 않습니다.";
            case UsernameNotFoundException usernameNotFoundException -> "존재하지 않는 사용자입니다.";
            case LockedException lockedException -> "계정이 잠겨 있습니다.";
            case null, default -> "로그인에 실패하였습니다.";
        };


        // 에러 정보를 담을 DTO나 Map 생성
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("status", HttpStatus.UNAUTHORIZED.value());
        responseBody.put("error", "Unauthorized");
        responseBody.put("message", message);

        //로그 저장(향후 추가)
        //실패 횟수 제한, IP 차단 로직 삽입 (향후 추가)

        // ObjectMapper로 JSON 변환 후 출력
        response.getWriter().write(objectMapper.writeValueAsString(responseBody));
    }
}
