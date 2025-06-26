package com.group_platform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableJpaAuditing //Auditing 기능을 쓰기 위해 필수
@EnableScheduling   //스케줄링 기능 활성화
@SpringBootApplication
public class GroupPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(GroupPlatformApplication.class, args);
    }

}
