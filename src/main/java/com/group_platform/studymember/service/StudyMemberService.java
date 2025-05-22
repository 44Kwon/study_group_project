package com.group_platform.studymember.service;

import com.group_platform.exception.BusinessLogicException;
import com.group_platform.exception.ExceptionCode;
import com.group_platform.studymember.entity.StudyMember;
import com.group_platform.studymember.repository.StudyMemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class StudyMemberService {

    private final StudyMemberRepository studyMemberRepository;

    public StudyMemberService(StudyMemberRepository studyMemberRepository) {
        this.studyMemberRepository = studyMemberRepository;
    }

    //리더인지 체크해서 적절한 에러처리(수정같은 처리 시 적절한 오류코드)
    public StudyMember validateLeaderWithUserIdAndStudyGroupId(Long userId, Long studyGroupId) {
        //studymember에서 userId와 groupid를 갖고있는 studymember를 가져와서 Leader인지 봐야함
        StudyMember studymember = studyMemberRepository.findByUserIdAndStudyGroupId(userId, studyGroupId)
                .orElseThrow(() -> new BusinessLogicException(ExceptionCode.MEMBER_NOT_FOUND));

        if (studymember.getRole() != StudyMember.InGroupRole.LEADER) {
            throw new BusinessLogicException(ExceptionCode.NO_PERMISSION);
        }

        return studymember;
    }

    //단순히 리더인지 체크(true,false)
    public boolean isLeader(Long userId, Long studyGroupId) {
        return studyMemberRepository.existsByUserIdAndStudyGroupIdAndRole(userId, studyGroupId, StudyMember.InGroupRole.LEADER);
    }

    //현재 그룹에서 활동중인 사람들의 숫자
    public int getCurrentMembers(Long studyGroupId) {
        return (int) studyMemberRepository.countByStudyGroupIdAndStatus(studyGroupId, StudyMember.ActiveStatus.ACTIVE);
    }
}
