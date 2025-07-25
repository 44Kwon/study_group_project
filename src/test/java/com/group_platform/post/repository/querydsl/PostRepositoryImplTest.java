package com.group_platform.post.repository.querydsl;

import com.group_platform.config.QueryDslConfig;
import com.group_platform.post.dto.PostResponseListDto;
import com.group_platform.post.dto.PostSortType;
import com.group_platform.post.entity.Post;
import com.group_platform.post.entity.PostType;
import com.group_platform.post.repository.PostRepository;
import com.group_platform.studymember.entity.StudyMember;
import com.group_platform.studymember.repository.StudyMemberRepository;
import com.group_platform.sutdygroup.entity.StudyGroup;
import com.group_platform.sutdygroup.repository.StudyGroupRepository;
import com.group_platform.user.entity.User;
import com.group_platform.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.util.Comparator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import(QueryDslConfig.class)
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PostRepositoryImplTest {
    @Autowired
    private PostRepositoryImpl postRepositoryImpl;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StudyGroupRepository studyGroupRepository;

    @Autowired
    private StudyMemberRepository studyMemberRepository;

    @BeforeAll
    void setUp(){
        postRepository.deleteAllInBatch();

        User user = User.builder()
                .username("testUser1")
                .nickname("testNickname1")
                .password("testPassword1")
                .build();
        User user2 = User.builder()
                .username("testUser2")
                .nickname("testNickname2")
                .password("testPassword2")
                .build();
        userRepository.saveAll(List.of(user, user2));

        Post post1 = Post.builder()
                .title("testTitle")
                .content("testContent")
                .user(user)
                .likeCount(10)
                .viewCount(5)
                .comment_count(3)
                .build();

        Post post2 = Post.builder()
                .title("internet post")
                .content("internet entity")
                .user(user)
                .likeCount(9)
                .viewCount(7)
                .comment_count(7)
                .build();
        Post post3 = Post.builder()
                .title("repository post")
                .content("admin")
                .user(user)
                .likeCount(8)
                .viewCount(3)
                .comment_count(8)
                .build();
        Post post4 = Post.builder()
                .title("user title")
                .content("description this is a user post")
                .user(user)
                .build();
        Post post5 = Post.builder()
                .title("title293879845")
                .content("content198948357")
                .user(user)
                .build();
        Post post6 = Post.builder()
                .title("안녕하세요 권우혁입니다")
                .content("내용입니다 내용")
                .user(user)
                .build();
        Post post7 = Post.builder()
                .title("이것은 제목입니다")
                .content("이것은 내용입니다")
                .user(user)
                .build();

        postRepository.saveAll(List.of(post1, post2, post3, post4, post5, post6, post7));
    }

    @DisplayName("공통게시글을 정렬 필터링하여 검색한다")
    @Test
    void getCommonPosts() {
        //when
        Page<PostResponseListDto> commentCountPosts = postRepositoryImpl.getCommonPosts(1L, PostType.GENERAL, PostSortType.COMMENT_COUNT, PageRequest.of(0, 3));
        Page<PostResponseListDto> likecCountPosts = postRepositoryImpl.getCommonPosts(1L, PostType.GENERAL, PostSortType.LIKE_COUNT, Pageable.ofSize(3).withPage(0));
        Page<PostResponseListDto> viewCountPosts = postRepositoryImpl.getCommonPosts(1L, PostType.GENERAL, PostSortType.VIEW_COUNT, Pageable.ofSize(3).withPage(0));

        //then
        assertThat(commentCountPosts.getContent()).hasSize(3)
                .extracting(PostResponseListDto::getTitle)
                .containsExactlyInAnyOrder("repository post", "internet post", "testTitle");

        assertThat(likecCountPosts.getContent()).hasSize(3)
                .extracting(PostResponseListDto::getTitle)
                .containsExactlyInAnyOrder("testTitle", "internet post", "repository post");

        assertThat(viewCountPosts.getContent()).hasSize(3)
                .extracting(PostResponseListDto::getTitle)
                .containsExactlyInAnyOrder("internet post", "testTitle", "repository post");

    }

    @DisplayName("스터디그룹 내에서 고정글을 조회한다.")
    @Test
    void getGroupPinnedPosts() {
        //given
        User user = User.builder()
                .username("권우혁")
                .nickname("테스트닉네임1")
                .password("testPassword1")
                .build();
        User user2 = User.builder()
                .username("권우혁2")
                .nickname("테스트닉네임2")
                .password("testPassword2")
                .build();

        userRepository.saveAll(List.of(user, user2));

        StudyGroup studyGroup = StudyGroup.builder()
                .name("testGroup")
                .description("testDescription")
                .build();
        studyGroupRepository.save(studyGroup);

        StudyMember studyMember1 = StudyMember.builder()
                .role(StudyMember.InGroupRole.LEADER)
                .studyGroup(studyGroup)
                .user(user)
                .build();

        StudyMember studyMember2 = StudyMember.builder()
                .role(StudyMember.InGroupRole.MEMBER)
                .studyGroup(studyGroup)
                .user(user2)
                .build();
        studyMemberRepository.saveAll(List.of(studyMember1, studyMember2));

        // 상단고정 고려할 것
        // 1순위 : 공지사항 + 고정
        // 2순위 : 고정
        Post pinnedPost1 = Post.builder()
                .title("공지사항 고정 제목")
                .content("공지사항 고정 내용")
                .user(user)
                .studyGroup(studyGroup)
                .postType(PostType.NOTICE)
                .isPinned(true)
                .build();

        Post pinnedPost2 = Post.builder()
                .title("고정글 리더")
                .content("고정글 리더")
                .user(user)
                .studyGroup(studyGroup)
                .isPinned(true)
                .build();

        Post pinnedPost3 = Post.builder()
                .title("고정글 멤버")
                .content("고정글 멤버")
                .user(user2)
                .studyGroup(studyGroup)
                .isPinned(true)
                .build();

        Post pinnedPost4 = Post.builder()
                .title("공지사항 제목")
                .content("공지사항 내용")
                .user(user)
                .postType(PostType.NOTICE)
                .studyGroup(studyGroup)
                .build();

        postRepository.saveAll(List.of(pinnedPost1, pinnedPost2, pinnedPost3, pinnedPost4));

        //when
        List<PostResponseListDto> groupPinnedPosts = postRepositoryImpl.getGroupPinnedPosts(user.getId(), studyGroup.getId());

        //then
        assertThat(groupPinnedPosts).hasSize(3)
                .extracting(PostResponseListDto::getTitle)
                .containsExactly("공지사항 고정 제목", "고정글 멤버", "고정글 리더");
    }


    @DisplayName("게시글 아이디를 이용하여서 게시글을 조회한다.")
    @Test
    void getSearchPosts() {
        //given
        List<Long> ids = postRepository.findAll().stream()
                .sorted(Comparator.comparing(Post::getId)) // ID 오름차순 정렬
                .limit(3)                                   // 앞 3개 추출
                .map(Post::getId)                           // ID만 추출
                .toList();
        //when
        List<PostResponseListDto> searchPosts = postRepositoryImpl.getSearchPosts(1L, ids, null);

        //then
        assertThat(searchPosts).hasSize(3)
                .extracting(PostResponseListDto::getTitle)
                .containsExactlyInAnyOrder("testTitle", "internet post", "repository post");
    }
}