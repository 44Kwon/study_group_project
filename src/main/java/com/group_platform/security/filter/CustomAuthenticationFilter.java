package com.group_platform.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.group_platform.security.dto.LoginDto;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;

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

    @Override
    //필터를 직접 custom해서 쓰면 성공처리를 직접 (세션생성 까지) 해줘야 하는듯 하다
    //원래는 security가 알아서 해주지만 커스텀을 쓰면 이렇게 직접 다 설정해줘야한다.
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
        // 세션 생성 강제 유도
        request.getSession(true);

        // 인증 객체 저장 (세션 기반 인증에서 반드시 필요)
//        SecurityContextHolder.getContext().setAuthentication(authResult);

        // 인증 정보 설정
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authResult);
        SecurityContextHolder.setContext(context);

        // 세션에 SecurityContext 저장 (여기가 핵심!!)
        request.getSession().setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                context
        );

        // 성공 핸들러 실행 (SecurityConfig에서 주입한 핸들러가 호출됨)
        this.getSuccessHandler().onAuthenticationSuccess(request, response, authResult);
    }
}
