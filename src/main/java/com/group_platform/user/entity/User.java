package com.group_platform.user.entity;

import com.group_platform.audit.BaseEntity;
import com.group_platform.comment.entity.Comment;
import com.group_platform.exception.BusinessLogicException;
import com.group_platform.exception.ExceptionCode;
import com.group_platform.post.bookmark.PostBookmark;
import com.group_platform.post.entity.Post;
import com.group_platform.qna.answer.entity.QnaAnswer;
import com.group_platform.qna.question.entity.QnaQuestion;
import com.group_platform.todo.entity.TodoUser;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //로그인 아이디
    @Column(nullable = false, updatable = false, unique = true, length = 50)
    private String username;

    //이메일은 필수가 아님
    @Column(unique = true)
    private String email;

    @Column(nullable = false, unique = true, length = 100)
    private String nickname;

    @Column(nullable = false, length = 100)
    private String password;

    //이미지 나중에
//    private String profileImageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default    //안붙이니 오류가 남 => 자동매퍼 사용하니 impl에서 빌더 사용해서 오류가 남
    private UserStatus userStatus = UserStatus.USER_ACTIVE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
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

    @OneToMany(mappedBy = "user", cascade = {CascadeType.PERSIST,CascadeType.REMOVE}, orphanRemoval = true)
    private List<QnaQuestion> questions = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = {CascadeType.PERSIST,CascadeType.REMOVE}, orphanRemoval = true)
    private List<QnaAnswer> answers = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = {CascadeType.PERSIST,CascadeType.REMOVE}, orphanRemoval = true)
    private List<TodoUser> todoUsers = new ArrayList<>();

    public void changeNickname(String nickname) {
        if (nickname == null || nickname.isBlank()) {
            throw new BusinessLogicException(ExceptionCode.USER_INVALID_NICKNAME);
        }
        this.nickname = nickname;
    }

    public void changeEmail(String email) {
        this.email = email;
    }

    public void changePassword(String password) {
        this.password = password;
    }

    //회원탈퇴
    public void withdraw(UserStatus userStatus) {
        if (userStatus == UserStatus.USER_ACTIVE) {
            this.userStatus = UserStatus.USER_WITHDRAW;
        }
    }

    public void updateEncodingPassword(String encodingPassword) {
        this.password = encodingPassword;
    }
}