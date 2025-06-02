package com.group_platform.post.entity;

import com.group_platform.audit.BaseEntity;
import com.group_platform.comment.entity.Comment;
import com.group_platform.sutdygroup.entity.StudyGroup;
import com.group_platform.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "posts")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Post extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 300)
    private String title;

    //TEXT는 문자열 검색 시(Like 쿼리) 성능 문제
    @Column(columnDefinition = "TEXT",nullable = false)
    private String content;

    @Column(nullable = false)
    private int viewCount = 0;

    private int comment_count;

    //상단고정인지 아닌지
    private boolean isPinned;

    //좋아요 수 캐싱
    private int likeCount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    @Setter
    private PostType postType = PostType.GENERAL; //일반

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "study_group_id")
    private StudyGroup studyGroup;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Comment> comments = new ArrayList<>();

    //연관관계 메서드
    //공통 게시글 작성시
    public void addCommonPost(User user) {
        this.user = user;;
    }

    //그룹 내 게시글 작성시
    public void addGroupPost(User user, StudyGroup studyGroup) {
        this.user = user;
        this.studyGroup = studyGroup;
    }

    public void changeTitle (String title) {
        this.title = title;
    }

    public void changeContent (String content) {
        this.content = content;
    }

    public void changePostType (PostType postType) {
        this.postType = postType;
    }

    public void changePinned (boolean pinned) {
        isPinned = pinned;
    }
}
