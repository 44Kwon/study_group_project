package com.group_platform.sutdygroup.entity;

import com.group_platform.audit.BaseEntity;
import com.group_platform.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "study_groups")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudyGroup extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, length = 500)
    private String description;

    //처음 생성 시 1로
    @Column(nullable = false)
    private int currentMembers = 1;

    private int maxMembers;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GroupStatus status = GroupStatus.ACTIVE;

    public enum GroupStatus {
        ACTIVE, INACTIVE, CLOSED
    }

    private String imageUrl; // 그룹의 이미지 URL
}
