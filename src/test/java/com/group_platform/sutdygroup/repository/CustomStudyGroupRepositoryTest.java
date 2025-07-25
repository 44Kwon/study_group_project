package com.group_platform.sutdygroup.repository;

import com.group_platform.post.repository.elasticsearch.PostSearchRepository;
import com.group_platform.sutdygroup.dto.StudyGroupDto;
import com.group_platform.sutdygroup.entity.StudyGroup;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class CustomStudyGroupRepositoryTest {

    @Autowired
    private StudyGroupRepository studyGroupRepository;

    @MockitoBean
    private PostSearchRepository postSearchRepository;

    @BeforeEach
    void setUp() {
        // 테스트 전 데이터 초기화
        studyGroupRepository.deleteAll();
    }

    @DisplayName("QueryDsl을 사용한 검색쿼리 테스트")
    @Test
    void test() {
        //given
        StudyGroup studyGroup1 = StudyGroup.builder()
                .name("studyName1")
                .description("studyDescription1")
                .build();
        StudyGroup studyGroup2 = StudyGroup.builder()
                .name("studyName2")
                .description("studyDescription2")
                .build();
        StudyGroup studyGroup3 = StudyGroup.builder()
                .name("studyName3")
                .description("studyDescription3")
                .build();
        StudyGroup studyGroup4 = StudyGroup.builder()
                .name("studyName4")
                .description("studyDescription4")
                .build();
        StudyGroup studyGroup5 = StudyGroup.builder()
                .name("studyName5")
                .description("studyDescription5")
                .build();

        studyGroupRepository.saveAll(List.of(studyGroup1,studyGroup2,studyGroup3,studyGroup4,studyGroup5));

        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "id"));

        //when
        Page<StudyGroupDto.ResponseList> result1 = studyGroupRepository.searchStudyGroups("studyName5", pageable);
        Page<StudyGroupDto.ResponseList> result2 = studyGroupRepository.searchStudyGroups("studyname", pageable);
        Page<StudyGroupDto.ResponseList> result3 = studyGroupRepository.searchStudyGroups("study", pageable);
        Page<StudyGroupDto.ResponseList> result4 = studyGroupRepository.searchStudyGroups(null, pageable);
        System.out.println(result1.getContent());
        //then
        assertThat(result1.getTotalElements()).isEqualTo(1);
        assertThat(result2.getTotalElements()).isEqualTo(5);
        assertThat(result3.getTotalElements()).isEqualTo(5);
        assertThat(result4.getTotalElements()).isEqualTo(5);
    }
}