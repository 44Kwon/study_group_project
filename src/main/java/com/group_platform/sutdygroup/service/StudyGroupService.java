package com.group_platform.sutdygroup.service;

import com.group_platform.category.entity.CategoryType;
import com.group_platform.exception.BusinessLogicException;
import com.group_platform.exception.ExceptionCode;
import com.group_platform.studymember.dto.StudyMemberDto;
import com.group_platform.studymember.entity.StudyMember;
import com.group_platform.studymember.repository.StudyMemberRepository;
import com.group_platform.studymember.service.StudyMemberService;
import com.group_platform.sutdygroup.dto.StudyGroupDto;
import com.group_platform.sutdygroup.dto.StudyGroupResponseDto;
import com.group_platform.sutdygroup.entity.StudyGroup;
import com.group_platform.sutdygroup.mapper.StudyGroupMapper;
import com.group_platform.sutdygroup.repository.StudyGroupRepository;
import com.group_platform.user.entity.User;
import com.group_platform.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class StudyGroupService {
    private final StudyGroupRepository studyGroupRepository;
    private final StudyGroupMapper studyGroupMapper;
    private final UserService userService;
    private final StudyMemberService studyMemberService;
    private final StudyMemberRepository studyMemberRepository;

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

        //그룹장만 업데이트 가능하다(그룹원인지, 그룹장인지 까지 체크, 내부에서 리더유저가 탈퇴했는지 여부도 체크중)
        studyMemberService.validateLeaderWithUserIdAndStudyGroupId(userId, studyGroup.getId());


        // 최대 인원 변경 시 현재 인원보다 적으면 에러처리
        // 필드에서 현재 인원수를 가져오는 중(수정해야 함 나중에)
        if(updateRequest.getMaxMembers() != null && updateRequest.getMaxMembers() < studyGroup.getCurrentMembers()) {
            throw new BusinessLogicException(ExceptionCode.INVALID_MAX_CAPACITY);
        }

        Optional.ofNullable(updateStudyGroup.getName())
                .ifPresent(studyGroup::changeName);
        Optional.ofNullable(updateStudyGroup.getDescription())
                .ifPresent(studyGroup::changeDescription);
        Optional.ofNullable(updateRequest.getMaxMembers())
                .ifPresent(studyGroup::changeMaxMembers);
        Optional.ofNullable(updateRequest.getType())
                .ifPresent(studyGroup::changeGroupType);

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

        //그룹장인지 체크할 것(단순히 체크 변경,삭제에 대해서 할 수 있게 버튼을 주기 위해서)
        boolean isOwner = studyMemberService.isLeader(userId, groupId);
        StudyGroupResponseDto studyGroupResponseDto = studyGroupMapper.studyGroupToStudyGroupResponseDto(studyGroup);
        studyGroupResponseDto.setOwner(isOwner);
        return studyGroupResponseDto;
    }


    @Transactional(readOnly = true)
    //활성화인 그룹 전부 가져오기
    public Page<StudyGroupDto.ResponseList> getActiveAllGroups(CategoryType categoryType, String sort, Pageable pageable) {
        Page<StudyGroup> result;

        //향후 enum으로 바꿀것
        if ("memberCount".equals(sort)) {
            pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(Sort.Direction.DESC, "currentMembers"));
        } else {
            // 최신순 정렬 (기본값)
            pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(Sort.Direction.DESC, "createdAt"));
        }

        //Active 상태인 그룹만 가져오기
        if (categoryType != null) {
            result = studyGroupRepository.findByTypeAndStatus(categoryType, StudyGroup.GroupStatus.ACTIVE, pageable);
        } else {
            result = studyGroupRepository.findByStatus(StudyGroup.GroupStatus.ACTIVE,pageable);
        }

        //페이징 상태에서 dto로 바꾸기
        return result.map(studyGroupMapper::studyGroupToStudyGroupResponseListDto);
    }

    @Transactional(readOnly = true)
    public Page<StudyGroupDto.ResponseList> searchGroup(String keyword, Pageable pageable) {
        //querydsl을 통해 dto로 조회처리
        return studyGroupRepository.searchStudyGroups(keyword, pageable);
    }


    //그룹삭제처리(상태변경)
    public void deleteGroup(Long userId, Long groupId) {
        deleteMyGroup(userId, groupId);
    }

    //트랜잭션 때문에 private
    private void deleteMyGroup(Long userId, Long groupId) {
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


    //그룹에 가입
    public void joinGroup(Long userId, Long groupId) {
        //그룹 검증
        StudyGroup studyGroup = validateByGroupId(groupId);

        //비활성화 그룹은 막기
        if (studyGroup.getStatus() != StudyGroup.GroupStatus.ACTIVE) {
            throw new BusinessLogicException(ExceptionCode.GROUP_DISABLED);
        }

        //만약 현재 유저수가 꽉찼으면 가입 막기
        if (studyGroup.getCurrentMembers() >= studyGroup.getMaxMembers()) {
            throw new BusinessLogicException(ExceptionCode.GROUP_FULL);
        }

        //가입 되어있는 사람인지 체크
        if (studyMemberRepository.existsByUserIdAndStudyGroupIdAndStatus(userId, groupId, StudyMember.ActiveStatus.ACTIVE)) {
            throw new BusinessLogicException(ExceptionCode.ALREADY_A_MEMBER);
        }

        //유저 검증
        User user = userService.validateUserWithUserId(userId);

        //그룹원 생성
        //연관관계설정(만약 user쪽에 양방향 걸거면 수정해줘야함, 위에 그룹 만드는것도 마찬가지)
        StudyMember studyMember = studyGroup.addStudyMember(user, StudyMember.InGroupRole.MEMBER);

        studyMemberRepository.save(studyMember);
    }

    //그룹 탈퇴
    //그룹 멤버 탈퇴 시 -> 그룹장이라면 그 다음 가입 멤버가 그룹장이 될 수 있도록 처리
    public void leaveGroup(Long userId, Long groupId) {
        // 그룹 검증
        StudyGroup studyGroup = validateByGroupId(groupId);

        // 1명일때는 그룹방 삭제처리
        if (studyGroup.getCurrentMembers() < 2) {
            deleteMyGroup(userId, groupId);
            return;
        }

        // 그룹원인지
        StudyMember studyMember = studyMemberService.validateMemberWithUserId(userId, groupId, StudyMember.ActiveStatus.ACTIVE);

        // 삭제처리(더티체킹)
        studyGroup.minusStudyGroupNumber();
        studyMember.changeStatus(StudyMember.ActiveStatus.INACTIVE);

        // 그룹장이면 탈퇴 시 넘겨주기
        if (studyMember.getRole() == StudyMember.InGroupRole.LEADER) {
            StudyMember nextLeader = studyMemberRepository.findFirstByStudyGroupIdAndStatusAndIdNotOrderByJoinDateAsc(
                            groupId, StudyMember.ActiveStatus.ACTIVE, studyMember.getId())
                    .orElseThrow(() -> new BusinessLogicException(ExceptionCode.MEMBER_NOT_FOUND));

            //더티체킹으로 처리
            nextLeader.changeRole(StudyMember.InGroupRole.LEADER);
        }
    }

    //그룹원 전부 보여주기
    @Transactional(readOnly = true)
    public List<StudyMemberDto.Response> getAllMembers(Long groupId) {
        StudyGroup studyGroup = validateByGroupId(groupId);
        //나중에 그룹장을 제일 위에다가 처리해보기
        return studyMemberService.getGroupMembers(groupId);
    }

    //그룹장 넘겨주기
    public void giveLeader(Long userId, Long groupId, Long newLeaderId) {
        StudyGroup studyGroup = validateByGroupId(groupId);

        StudyMember oldLeader = studyMemberService.validateLeaderWithUserIdAndStudyGroupId(userId, groupId);
        StudyMember newLeader = studyMemberService.validateMemberWithUserId(newLeaderId, groupId, StudyMember.ActiveStatus.ACTIVE);

        oldLeader.giveLeaderRole(newLeader);
    }

    //그룹휴면처리=> 그룹 휴면처리를 하면 더이상 그룹원 더 받을 수 없음.(검색 시에도 안나타나게 할 지는 추후결정)
    public void deActivateGroup(Long userId, Long groupId) {
        StudyGroup studyGroup = validateByGroupId(groupId);
        studyMemberService.validateLeaderWithUserIdAndStudyGroupId(userId, groupId);
        studyGroup.changeGroupStatus(StudyGroup.GroupStatus.INACTIVE);
    }

    //그룹 다시 active처리
    public void activateGroup(Long userId, Long groupId) {
        StudyGroup studyGroup = validateByGroupId(groupId);
        studyMemberService.validateLeaderWithUserIdAndStudyGroupId(userId, groupId);
        studyGroup.changeGroupStatus(StudyGroup.GroupStatus.ACTIVE);
    }

    private StudyGroup validateByGroupId(Long groupId) {
        return studyGroupRepository.findById(groupId)
                .orElseThrow(()-> new BusinessLogicException(ExceptionCode.GROUP_NOT_EXIST));
    }
}
