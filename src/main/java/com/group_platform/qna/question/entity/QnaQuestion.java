package com.group_platform.qna.question.entity;

import com.group_platform.audit.BaseEntity;
import com.group_platform.qna.answer.entity.QnaAnswer;
import com.group_platform.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "qna_questions")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QnaQuestion extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long questionId;

    @Column(nullable = false, length = 500)
    private String title;

    @Column(nullable = false, length = 1000)
    private String content;

    @Column(nullable = false)
    private int viewCount = 0;

    @Column(nullable = false)
    private int answerCount = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "qnaQuestion", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<QnaAnswer> QnaAnswers = new ArrayList<>();
}
