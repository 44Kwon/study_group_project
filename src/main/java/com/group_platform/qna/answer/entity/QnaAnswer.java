package com.group_platform.qna.answer.entity;

import com.group_platform.audit.BaseEntity;
import com.group_platform.qna.question.entity.QnaQuestion;
import com.group_platform.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "qna_answers")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QnaAnswer extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long answerId;

    @Column(nullable = false, length = 1000)
    private String content;

    private boolean isAccepted;

    @Column(nullable = false)
    private int likeCount = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "qna_question_id")
    private QnaQuestion qnaQuestion;
}
