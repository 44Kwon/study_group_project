package com.group_platform.comment.repository;

import com.group_platform.comment.dto.CommentDto;
import com.group_platform.comment.entity.Comment;
import com.group_platform.config.QueryDslConfig;
import com.group_platform.user.entity.User;
import com.group_platform.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import(QueryDslConfig.class)
class CommentRepositoryImplTest {

    @Autowired
    private CommentRepositoryImpl commentRepositoryImpl;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private UserRepository userRepository;

    @DisplayName("대댓글이 존재하는 지 확인하는 테스트")
    @Test
    void existsByRelies() {
        //given
        User user = User.builder()
                .username("username1")
                .nickname("nickname1")
                .password("password1")
                .build();

        userRepository.save(user);

        Comment content1 = Comment.builder()
                .content("content1")
                .user(user)
                .build();

        Comment content2 = Comment.builder()
                .content("content2")
                .user(user)
                .build();

        Comment content3 = Comment.builder()
                .content("content3")
                .parent(content1)
                .user(user)
                .build();

        Comment content4 = Comment.builder()
                .content("content4")
                .parent(content1)
                .user(user)
                .build();
        commentRepository.saveAll(List.of(content1, content2, content3, content4));
        //when
        // 대댓글이 존재하지 않으면 아예 조회되지 않음
        List<CommentDto.IsCommentHaveReplyDto> isCommentHaveReplyDtos = commentRepositoryImpl.existsByRelies(List.of(content1.getId(), content2.getId()));

        //then
        assertThat(isCommentHaveReplyDtos).hasSize(1)
                .extracting(CommentDto.IsCommentHaveReplyDto::getReplyCount)
                .containsExactly(2L);
    }
}