package com.group_platform.comment.repository;

import com.group_platform.comment.entity.Comment;
import com.group_platform.post.entity.Post;
import com.group_platform.post.repository.PostRepository;
import com.group_platform.post.repository.elasticsearch.PostSearchRepository;
import com.group_platform.user.entity.User;
import com.group_platform.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

//@DataJpaTest
@SpringBootTest
@Transactional
@ActiveProfiles("test")
class CommentRepositoryTest {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    //도커에 ElasticSearch가 띄워져있지 않아서 오류 발생. PostSearchRepository는 테스트에서 사용하지 않음(모킹처리)
    @MockitoBean
    private PostSearchRepository postSearchRepository;

    @DisplayName("댓글 페이징에 사용되는 해당 게시글에 담긴 댓글과 댓글 사용자에 대한 데이터를 조회")
    @Test
    void findAllByPostIdAndParentIdIsNull() {
        //given
        User user1 = User.builder()
                .username("username1")
                .nickname("nickname1")
                .password("password1")
                .build();
        User user2 = User.builder()
                .username("username2")
                .nickname("nickname2")
                .password("password2")
                .build();
        User user3 = User.builder()
                .username("username3")
                .nickname("nickname3")
                .password("password3")
                .build();
        User user4 = User.builder()
                .username("username4")
                .nickname("nickname4")
                .password("password4")
                .build();

        userRepository.saveAll(List.of(user1, user2, user3, user4));

        Post post = Post.builder()
                .title("게시글 제목")
                .content("게시글 내용")
                .user(user1)
                .build();
        postRepository.save(post);

        Comment comment1 = Comment.builder()
                .content("댓글 내용 1")
                .user(user2)
                .post(post)
                .build();
        Comment comment2 = Comment.builder()
                .content("댓글 내용 2")
                .user(user3)
                .post(post)
                .build();

        Comment comment3 = Comment.builder()
                .content("댓글 내용 3")
                .user(user4)
                .post(post)
                .build();
        Comment comment4 = Comment.builder()
                .content("댓글 내용 4")
                .user(user1)
                .post(post)
                .build();

        Comment comment5 = Comment.builder()
                .content("댓글 내용 5")
                .user(user2)
                .post(post)
                .build();

        commentRepository.saveAll(List.of(comment1, comment2, comment3, comment4, comment5));
        //when
        Pageable pageable1 = Pageable.ofSize(3).withPage(1); // 두번째 페이지, 3개씩
        Pageable pageable2 = Pageable.ofSize(10).withPage(0); // 첫 페이지, 10개씩

        Page<Comment> comments1 = commentRepository.findAllByPostIdAndParentIdIsNull(post.getId(), pageable1);
        Page<Comment> comments2 = commentRepository.findAllByPostIdAndParentIdIsNull(post.getId(), pageable2);

        //then
        // pageable1과 pageable2의 테스트를 따로하고, 각각의 댓글 내용과 user nickname을 검증
        assertThat(comments1.getTotalElements()).isEqualTo(5);
        assertThat(comments1.getContent()).hasSize(2)
                .extracting(Comment::getContent, comment-> comment.getUser().getNickname())
                .containsExactlyInAnyOrder(
                        org.assertj.core.api.Assertions.tuple("댓글 내용 4", user1.getNickname()),
                        org.assertj.core.api.Assertions.tuple("댓글 내용 5", user2.getNickname())
                );

        assertThat(comments2.getTotalElements()).isEqualTo(5);
        assertThat(comments2.getContent()).hasSize(5)
                .extracting(Comment::getContent, comment -> comment.getUser().getNickname())
                .containsExactlyInAnyOrder(
                        org.assertj.core.api.Assertions.tuple("댓글 내용 1", user2.getNickname()),
                        org.assertj.core.api.Assertions.tuple("댓글 내용 2", user3.getNickname()),
                        org.assertj.core.api.Assertions.tuple("댓글 내용 3", user4.getNickname()),
                        org.assertj.core.api.Assertions.tuple("댓글 내용 4", user1.getNickname()),
                        org.assertj.core.api.Assertions.tuple("댓글 내용 5", user2.getNickname())
                );
    }
}