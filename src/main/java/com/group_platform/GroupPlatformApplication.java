package com.group_platform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing //Auditing 기능을 쓰기 위해 필수
@SpringBootApplication
public class GroupPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(GroupPlatformApplication.class, args);
    }

}
