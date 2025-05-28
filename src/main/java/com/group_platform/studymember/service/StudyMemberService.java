package com.group_platform.studymember.service;

import com.group_platform.exception.BusinessLogicException;
import com.group_platform.exception.ExceptionCode;
import com.group_platform.studymember.dto.StudyMemberDto;
import com.group_platform.studymember.entity.StudyMember;
import com.group_platform.studymember.mapper.StudyMemberMapper;
import com.group_platform.studymember.repository.StudyMemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class StudyMemberService {

    private final StudyMemberRepository studyMemberRepository;
    private final StudyMemberMapper studyMemberMapper;

    public StudyMemberService(StudyMemberRepository studyMemberRepository, StudyMemberMapper studyMemberMapper) {
        this.studyMemberRepository = studyMemberRepository;
        this.studyMemberMapper = studyMemberMapper;
    }

    //리더인지 체크해서 적절한 에러처리(수정같은 처리 시 적절한 오류코드)
    public StudyMember validateLeaderWithUserIdAndStudyGroupId(Long userId, Long studyGroupId) {
        //studymember에서 userId와 groupid를 갖고있는 studymember를 가져와서 Leader인지 봐야함
        StudyMember studymember = studyMemberRepository.findByUser_IdAndStudyGroup_IdAndStatus(userId, studyGroupId, StudyMember.ActiveStatus.ACTIVE)
                .orElseThrow(() -> new BusinessLogicException(ExceptionCode.MEMBER_NOT_FOUND));

        if (studymember.getRole() != StudyMember.InGroupRole.LEADER) {
            throw new BusinessLogicException(ExceptionCode.NO_PERMISSION);
        }

        return studymember;
    }

    //단순히 리더인지 체크(true,false)
    public boolean isLeader(Long userId, Long studyGroupId) {
        return studyMemberRepository.existsByUser_IdAndStudyGroup_IdAndRoleAndStatus(userId, studyGroupId, StudyMember.InGroupRole.LEADER, StudyMember.ActiveStatus.ACTIVE);
    }

    //현재 그룹에서 활동중인 사람들의 숫자
    public int getCurrentMembers(Long studyGroupId) {
        return (int) studyMemberRepository.countByStudyGroup_IdAndStatus(studyGroupId, StudyMember.ActiveStatus.ACTIVE);
    }

    //그룹원인지
    public StudyMember validateMemberWithUserId(Long userId, Long groupId, StudyMember.ActiveStatus status) {
        return studyMemberRepository.findByUser_IdAndStudyGroup_IdAndStatus(userId, groupId, status)
                .orElseThrow(() -> new BusinessLogicException(ExceptionCode.MEMBER_NOT_FOUND));
    }

    //그룹 활동중인 모든 멤버를 반환
    public List<StudyMemberDto.Response> getGroupMembers(Long groupId) {
        return studyMemberMapper.StudyMembersToResponse(studyMemberRepository.findAllByStudyGroupIdAndStatusOrderByJoinDateAsc(groupId, StudyMember.ActiveStatus.ACTIVE));
    }
}
