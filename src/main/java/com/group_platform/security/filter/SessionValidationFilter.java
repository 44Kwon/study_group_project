package com.group_platform.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
@Component
// 요청마다 없는 유저에 대해 redis에 남아있는 세션으로 접근 시 세션 무효화하는 필터
public class SessionValidationFilter extends OncePerRequestFilter {
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof UserDetails) {
            String username = ((UserDetails) auth.getPrincipal()).getUsername();

            try {
                userDetailsService.loadUserByUsername(username);
                // 유저가 존재하면 정상 진행
            } catch (UsernameNotFoundException e) {
                // 유저가 DB에 없으면 세션 무효화 및 인증 클리어
                SecurityContextHolder.clearContext();
                HttpSession session = request.getSession(false);
                if (session != null) {
                    session.invalidate();
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}
