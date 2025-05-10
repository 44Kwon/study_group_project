package com.group_platform.user.entity;

import com.group_platform.audit.BaseEntity;
import com.group_platform.comment.entity.Comment;
import com.group_platform.post.bookmark.PostBookmark;
import com.group_platform.post.entity.Post;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, updatable = false)
    private String email;

    @Column(nullable = false, unique = true, length = 100)
    private String nickname;

    @Column(nullable = false, length = 100)
    private String password;

    private String profileImageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus userStatus = UserStatus.USER_ACTIVE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.USER;

    @Getter
    public enum UserStatus {
        USER_ACTIVE ("활동중인 계정"),
        USER_SLEEP("휴면 계정"),
        USER_WITHDRAW("탈퇴한 계정");

        private String description;

        UserStatus(String description) {
            this.description = description;
        }
    }

    @OneToMany(mappedBy = "user", cascade = {CascadeType.PERSIST,CascadeType.REMOVE}, orphanRemoval = true)
    private List<Post> posts = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = {CascadeType.PERSIST,CascadeType.REMOVE}, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = {CascadeType.PERSIST,CascadeType.REMOVE}, orphanRemoval = true)
    private List<PostBookmark> postBookmarks = new ArrayList<>();
}