package com.group_platform.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.group_platform.security.dto.LoginDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;

// 로그인 api 처리를 Security가 인증 필터 수준에서 처리 하게 하는 방식
public class CustomAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    public CustomAuthenticationFilter(AuthenticationManager authenticationManager) {
        super.setAuthenticationManager(authenticationManager);
        // 로그인 URL 변경 가능 (기본은 /login)
//        setFilterProcessesUrl("/api/login"); securityconfig에서 적게 해뒀음
    }


    // 인증 요청 시도 (username, password 파싱)
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {

        try {
            // JSON 요청 바디를 읽어서 LoginRequest 객체로 변환 (Jackson 사용)
            ObjectMapper objectMapper = new ObjectMapper();
            //request.getInputStream()는 요청 본문을 읽어옴
            //objectMapper.readValue(...)은 JSON을 Java 객체로 바꿈
            LoginDto loginRequest = objectMapper.readValue(request.getInputStream(), LoginDto.class);

            UsernamePasswordAuthenticationToken authRequest =
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword());

            //나중에 더 찾아보기
            setDetails(request, authRequest);

            return this.getAuthenticationManager().authenticate(authRequest);

        } catch (IOException e) {
//            System.out.println("오류야오류");
            throw new AuthenticationServiceException(e.getMessage(), e);
        }
    }
}
