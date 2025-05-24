package com.group_platform.user.repository;

import com.group_platform.user.entity.Role;
import com.group_platform.user.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @DisplayName("유저 상태가 USER_WITHDRAW인 것만 가져오기(회원 삭제 스케줄링시 사용")
    @Test
    void findExpiredWithdrawnUsers() {
        //given
        User user1 = User.builder()
                .username("john")
                .nickname("john")
                .password("<PASSWORD>")
                .role(Role.USER)
                .userStatus(User.UserStatus.USER_WITHDRAW)
                .build();
        User user2 = User.builder()
                .username("park")
                .nickname("park")
                .password("<PASSWORD>")
                .role(Role.USER)
                .userStatus(User.UserStatus.USER_WITHDRAW)
                .build();
        User user3 = User.builder()
                .username("kim")
                .nickname("kim")
                .password("<PASSWORD>")
                .role(Role.USER)
                .userStatus(User.UserStatus.USER_ACTIVE)
                .build();
        User user4 = User.builder()
                .username("kwon")
                .nickname("kwon")
                .password("<PASSWORD>")
                .role(Role.USER)
                .userStatus(User.UserStatus.USER_ACTIVE)
                .build();
        userRepository.saveAll(List.of(user1, user2, user3, user4));

        //when
        List<User> expiredWithdrawnUsers = userRepository.findExpiredWithdrawnUsers(LocalDateTime.now().plusDays(1));

        //then
        assertThat(expiredWithdrawnUsers).hasSize(2)
                .extracting(User::getUsername)
                .containsExactlyInAnyOrder("john", "park");
    }

}