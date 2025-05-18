package com.group_platform.user.repository;

import com.group_platform.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String nickname);
    boolean existsByNickname(String nickname);
    boolean existsByEmail(String email);

    //회원 삭제 시 (유저 상태가 USER_WITHDRAW인 것만 가져와서 삭제처리)
    @Query("SELECT u FROM User u WHERE u.userStatus = 'USER_WITHDRAW' AND u.updatedAt < :cutoff")
    List<User> findExpiredWithdrawnUsers(@Param("cutoff") LocalDateTime cutoff);
}
