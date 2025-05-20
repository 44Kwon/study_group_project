package com.group_platform.security.dto;

import com.group_platform.user.entity.Role;
import com.group_platform.user.entity.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

// record는 불변 DTO를 만드는 자바14이상 문법
// User 객체를 담아 UserDetails를 구현하는 용도로 쓰임
// 현재 user를 계속 끌고다닌다 => 필요 필드만 가져가게 수정할것
public class CustomUserDetails implements UserDetails {

    @Getter
    private final Long id;
    private final String username;
    private final String password;
    private final Role role;

    public CustomUserDetails(Long id, String username, String password, Role role) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
    }

    @Override
    //사용자의 권한 목록을 반환
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role.name()));
//        return user.getRole().stream() // 예: Set<Role>
//                .map(role -> new SimpleGrantedAuthority(role.name()))
//                .collect(Collectors.toList());
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    //로그인할 때 사용자가 입력하는 ID
    public String getUsername() {
        return username;
    }

    //대부분 정책 사용을 안하기 때문에 true로 설정
    @Override
    //계정 만료 여부
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    //계정이 잠겨있지 않은지 여부
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    //비밀번호가 만료되지 않았는지 여부
    //비밀번호 주기적 변경 정책이 있을 때 만료 여부 체크(true면 비밀번호 유효, false면 만료되어 재설정 필요)
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    //계정이 활성화되어 있는지 여부
    public boolean isEnabled() {
        return true;
    }

}
