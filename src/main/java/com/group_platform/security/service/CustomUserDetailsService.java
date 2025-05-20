package com.group_platform.security.service;

import com.group_platform.security.dto.CustomUserDetails;
import com.group_platform.user.entity.User;
import com.group_platform.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    //loadUserByUsername에서 비밀번호를 직접 검증하려고 하면 안 됨
    //(비밀번호 검증 로직을 여기 넣으면 Spring Security가 제공하는 인증 체계를 깨는 셈이라서 보통 하지 않는다)
    //비밀번호 검증(입력한 비밀번호와 DB 비밀번호 비교)은 Spring Security 내부의 AuthenticationManager가 맡아서 처리
    //loadUserByUsername은 오직 사용자 존재 여부만 확인
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("해당 사용자가 존재하지 않습니다."));

        if (user.getUserStatus() == User.UserStatus.USER_WITHDRAW) {
            throw new DisabledException("탈퇴한 사용자입니다.");
        }

        return new CustomUserDetails(user.getId(), user.getUsername(), user.getPassword(), user.getRole());
    }
}
