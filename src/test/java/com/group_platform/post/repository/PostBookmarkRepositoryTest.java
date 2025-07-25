package com.group_platform.post.repository;

import com.group_platform.config.QueryDslConfig;
import com.group_platform.post.bookmark.PostBookmark;
import com.group_platform.post.entity.Post;
import com.group_platform.user.entity.User;
import com.group_platform.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@Import(QueryDslConfig.class)
class PostBookmarkRepositoryTest {
    @Autowired
    private PostBookmarkRepository postBookmarkRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @DisplayName("북마크 조회시 내가 북마크한 게시글을 조회한다")
    @Test
    void findPostMyBookmark() {
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
        userRepository.saveAll(List.of(user1, user2));

        Post post1 = Post.builder()
                .title("Post Title 1")
                .content("Post Content 1")
                .user(user1)
                .build();

        Post post2 = Post.builder()
                .title("Post Title 2")
                .content("Post Content 2")
                .user(user2)
                .build();
        postRepository.saveAll(List.of(post1, post2));

        PostBookmark Bookmark1 = PostBookmark.builder()
                .build();
        Bookmark1.bookmarkPost(post1, user1);

        PostBookmark Bookmark2 = PostBookmark.builder()
                .build();
        Bookmark2.bookmarkPost(post2, user1);

        PostBookmark Bookmark3 = PostBookmark.builder()
                .build();
        Bookmark3.bookmarkPost(post1, user2);
        postBookmarkRepository.saveAll(List.of(Bookmark1, Bookmark2, Bookmark3));

        //when
        Page<PostBookmark> postMyBookmark1 = postBookmarkRepository.findPostMyBookmark(user1.getId(), PageRequest.of(0, 10));
        Page<PostBookmark> postMyBookmark2 = postBookmarkRepository.findPostMyBookmark(user2.getId(), PageRequest.of(0, 10));

        //then
        assertThat(postMyBookmark1.getContent()).hasSize(2)
                .extracting(PostBookmark::getPost)
                .extracting(Post::getTitle)
                .containsExactlyInAnyOrder("Post Title 1", "Post Title 2");

        assertThat(postMyBookmark2.getContent()).hasSize(1)
                .extracting(PostBookmark::getPost)
                .extracting(Post::getTitle)
                .containsExactlyInAnyOrder("Post Title 1");
    }
}