package com.group_platform.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
//회원 삭제 스케줄러(회원 탈퇴한지 30일 지나면 삭제한다)
public class UserCleanupScheduler {
    private final UserService userService;

    @Scheduled(cron = "0 0 1 * * ?")  // 매일 새벽 1시에 실행
    public void cleanupUsers() {
        userService.deleteExpiredUsers();
    }
}
