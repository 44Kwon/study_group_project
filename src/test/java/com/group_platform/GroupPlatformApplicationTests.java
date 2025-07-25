package com.group_platform;

import com.group_platform.post.repository.elasticsearch.PostSearchRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@ActiveProfiles("test")
class GroupPlatformApplicationTests {

    @MockitoBean
    private PostSearchRepository postSearchRepository;

    @Test
    void contextLoads() {
    }

}
