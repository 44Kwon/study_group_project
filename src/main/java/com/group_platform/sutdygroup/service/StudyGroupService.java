package com.group_platform.sutdygroup.service;

import com.group_platform.exception.BusinessLogicException;
import com.group_platform.exception.ExceptionCode;
import com.group_platform.studymember.entity.StudyMember;
import com.group_platform.studymember.service.StudyMemberService;
import com.group_platform.sutdygroup.dto.StudyGroupDto;
import com.group_platform.sutdygroup.dto.StudyGroupResponseDto;
import com.group_platform.sutdygroup.entity.StudyGroup;
import com.group_platform.sutdygroup.mapper.StudyGroupMapper;
import com.group_platform.sutdygroup.repository.StudyGroupRepository;
import com.group_platform.user.entity.User;
import com.group_platform.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class StudyGroupService {
    private final StudyGroupRepository studyGroupRepository;
    private final StudyGroupMapper studyGroupMapper;
    private final UserService userService;
    private final StudyMemberService studyMemberService;

    public Long createGroup(Long userId, StudyGroupDto.CreateRequest createRequest) {

        //user를 가져와서 연관관계설정(JPA사용이 (객체간 연관관계설정)이 성능저하의 원인이 되는 중. 어찌해야할지 차후대책)
        User user = userService.validateUserWithUserId(userId);


        //스터디 그룹 생성
        StudyGroup newStudyGroup = studyGroupMapper.createRequestToStudyGroup(createRequest);

        //스터디 멤버 생성(리더로)
        //연관관계 메서드 -> 멤버 생성하고, 양방향 연관관계 설정
        newStudyGroup.addStudyMember(user, StudyMember.InGroupRole.LEADER);

        StudyGroup savedGroup = studyGroupRepository.save(newStudyGroup);
        return savedGroup.getId();
    }

    public StudyGroupResponseDto updateGroup(Long userId, StudyGroupDto.UpdateRequest updateRequest) {
        StudyGroup updateStudyGroup = studyGroupMapper.updateRequestToStudyGroup(updateRequest);

        //그룹이 존재하는지 여부
        StudyGroup studyGroup = validateByGroupId(updateStudyGroup.getId());

        //그룹장만 업데이트 가능하다(그룹원인지, 그룹장인지 까지 체크)
        studyMemberService.validateLeaderWithUserIdAndStudyGroupId(userId, studyGroup.getId());


        // 최대 인원 변경 시 현재 인원보다 적으면 에러처리
        // 필드에서 현재 인원수를 가져오는 중(수정해야 함 나중에)
        if(updateRequest.getMaxMember() != null && updateRequest.getMaxMember() < studyGroup.getCurrentMembers()) {
            throw new BusinessLogicException(ExceptionCode.INVALID_MAX_CAPACITY);
        }

        Optional.ofNullable(updateStudyGroup.getName())
                .ifPresent(studyGroup::changeName);
        Optional.ofNullable(updateStudyGroup.getDescription())
                .ifPresent(studyGroup::changeDescription);
        Optional.ofNullable(updateRequest.getMaxMember())
                .ifPresent(studyGroup::changeMaxMembers);

        StudyGroupResponseDto studyGroupResponseDto = studyGroupMapper.studyGroupToStudyGroupResponseDto(studyGroup);
        studyGroupResponseDto.setOwner(true);

        return studyGroupResponseDto;
    }

    @Transactional(readOnly = true)
    public StudyGroupResponseDto getGroup(Long userId, Long groupId) {
        //그룹이 존재하는지 여부
        StudyGroup studyGroup = validateByGroupId(groupId);

        //그룹 상태에 따라 다르게 오류코드 설정
        if (studyGroup.getStatus() == StudyGroup.GroupStatus.INACTIVE) {
            throw new BusinessLogicException(ExceptionCode.GROUP_DISABLED);
        } else if (studyGroup.getStatus() == StudyGroup.GroupStatus.CLOSED) {
            throw new BusinessLogicException(ExceptionCode.GROUP_DELETED);
        }

        //그룹장인지 체크할 것(단순히 체크)
        boolean isOwner = studyMemberService.isLeader(userId, groupId);
        StudyGroupResponseDto studyGroupResponseDto = studyGroupMapper.studyGroupToStudyGroupResponseDto(studyGroup);
        studyGroupResponseDto.setOwner(isOwner);
        return studyGroupResponseDto;
    }

    //그룹휴면처리=> 그룹 휴면처리를 하면 더이상 그룹원 더 받을 수 없음.(검색 시에도 안나타나게 할 지는 추후결정)


    //그룹 다시 active처리


    //삭제처리(상태변경)
    public void deleteGroup(Long userId, Long groupId) {
        //그룹장인지 체크할지... 굳이?

        //그룹 존재하는지
        StudyGroup studyGroup = validateByGroupId(groupId);

        //삭제는 오직 그룹원이 한명일 때만
        //정확성을 위해서 직접 count로 인원수 가져오기
        int currentMembers = studyMemberService.getCurrentMembers(groupId);

        if (currentMembers > 1) {
            throw new BusinessLogicException(ExceptionCode.GROUP_DELETE_DINED_BY_EXISTING_MEMBERS);
        }

        // 리더인지 판별하고 가져오기
        StudyMember studyMember = studyMemberService.validateLeaderWithUserIdAndStudyGroupId(userId, groupId);

        // studymember 상태변경, studygroup 상태변경
        studyGroup.changeStatusToDelete(studyMember);
    }

    private StudyGroup validateByGroupId(Long groupId) {
        return studyGroupRepository.findById(groupId)
                .orElseThrow(()-> new BusinessLogicException(ExceptionCode.GROUP_NOT_EXIST));
    }
}
