package com.group_platform.comment.entity;

import com.group_platform.audit.BaseEntity;
import com.group_platform.post.entity.Post;
import com.group_platform.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "comments")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Comment extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 500)
    private String content;


    //대댓글을 위해 parent-child
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Comment parent;

    @OneToMany(mappedBy = "parent", cascade = {CascadeType.PERSIST,CascadeType.REMOVE}, orphanRemoval = true)
    @Builder.Default
    private List<Comment> children = new ArrayList<>();


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    //연관관계 메서드 댓글,대댓글
    public void addReply(Comment parent) {
        this.parent = parent;
        //굳이 양방향으로 걸어서 조회 쿼리치게 할 필요가 없다
//        parent.getChildren().add(this);
    }

    public void setPostWithComment(User user, Post post) {
        this.user = user;
        this.post = post;
        //굳이 양방향으로 걸어서 조회 쿼리치게 할 필요가 없다
    }

    public void changeContent(String content) {
        this.content = content;
    }
}
