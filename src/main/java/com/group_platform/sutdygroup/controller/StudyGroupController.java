package com.group_platform.sutdygroup.controller;

import com.group_platform.category.entity.CategoryType;
import com.group_platform.response.ResponseDto;
import com.group_platform.security.dto.CustomUserDetails;
import com.group_platform.studymember.dto.StudyMemberDto;
import com.group_platform.sutdygroup.dto.StudyGroupDto;
import com.group_platform.sutdygroup.dto.StudyGroupResponseDto;
import com.group_platform.sutdygroup.service.StudyGroupService;
import com.group_platform.util.UriComponent;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/study-group")
@RequiredArgsConstructor
@Validated
public class StudyGroupController {
    private final StudyGroupService studyGroupService;
    private final static String STUDY_GROUP_DEFAULT_URI = "/study-group";

    @PostMapping
    //스터디 그룹 생성
    public ResponseEntity<?> createStudyGroup(@AuthenticationPrincipal CustomUserDetails userDetails,
                                              @RequestBody @Valid StudyGroupDto.CreateRequest createRequest) {
        Long groupId = studyGroupService.createGroup(userDetails.getId(),createRequest);
        URI uri = UriComponent.createUri(STUDY_GROUP_DEFAULT_URI,groupId);
        return ResponseEntity.created(uri).build();
    }

    @PatchMapping("/{study-group-id}")
    public ResponseEntity<?> updateStudyGroup(@AuthenticationPrincipal CustomUserDetails userDetails,
                                              @RequestBody @Valid StudyGroupDto.UpdateRequest updateRequest,
                                              @PathVariable("study-group-id") @Positive Long groupId) {
        updateRequest.setId(groupId);
        StudyGroupResponseDto studyGroupResponseDto = studyGroupService.updateGroup(userDetails.getId(), updateRequest);
        return new ResponseEntity<>(
                new ResponseDto.SingleResponseDto<>(studyGroupResponseDto), HttpStatus.OK);
    }

    @GetMapping("/{study-group-id}")
    public ResponseEntity<?> getStudyGroup(@AuthenticationPrincipal CustomUserDetails userDetails,
                                           @PathVariable("study-group-id") @Positive Long groupId) {
        StudyGroupResponseDto studyGroupResponseDto = studyGroupService.getGroup(userDetails.getId(),groupId);
        return new ResponseEntity<>(
                new ResponseDto.SingleResponseDto<>(studyGroupResponseDto), HttpStatus.OK);
    }

    @DeleteMapping("/{study-group-id}")
    public ResponseEntity<?> deleteStudyGroup(@AuthenticationPrincipal CustomUserDetails userDetails,
                                              @PathVariable("study-group-id") Long groupId) {
        studyGroupService.deleteGroup(userDetails.getId(),groupId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping
    // 스터디 그룹 목록(메인페이지용) - 어떻게 보여줄지
    // 필터링용 카테고리 목록 게속 같이 보내주기
    public ResponseEntity<?> getActiveStudyGroups(@RequestParam(required = false) CategoryType categoryType,
                                                  @RequestParam(defaultValue = "LATEST") String sort, //향후 enum으로 바꾸기
                                                  @PageableDefault(size = 10) Pageable pageable) {  //Pageable로 받으면 알아서 0-based 처리해줌
        Page<StudyGroupDto.ResponseList> activeAllGroups = studyGroupService.getActiveAllGroups(categoryType, sort, pageable);
        List<StudyGroupDto.ResponseList> content = activeAllGroups.getContent();

        return new ResponseEntity<>(
                new ResponseDto.MultipleResponseDto<>(content, activeAllGroups), HttpStatus.OK);
    }


    // 그룹 휴면처리 => 그룹 휴면처리를 하면 더이상 그룹원을 받을 수 없고, 검색 시에도 나타나지 않게
    @PatchMapping("/{study-group-id}/inactive")
    public ResponseEntity<?> deactivateGroup(@AuthenticationPrincipal CustomUserDetails userDetails,
                                             @PathVariable("study-group-id") @Positive Long groupId) {
        studyGroupService.deActivateGroup(userDetails.getId(), groupId);
        return ResponseEntity.ok().build();
    }

    // 그룹 다시 active 처리
    @PatchMapping("/{study-group-id}/active")
    public ResponseEntity<?> activateGroup(@AuthenticationPrincipal CustomUserDetails userDetails,
                                           @PathVariable("study-group-id") @Positive Long groupId) {
        studyGroupService.activateGroup(userDetails.getId(), groupId);
        return ResponseEntity.ok().build();
    }
    

    /**
     * 그룹 멤버와 관련된 API들
     */

    @PostMapping("{study-group-id}/members")
    //그룹 멤버 가입
    public ResponseEntity<?> joinGroup(@AuthenticationPrincipal CustomUserDetails userDetails,
                                       @PathVariable("study-group-id") @Positive Long groupId) {
        studyGroupService.joinGroup(userDetails.getId(),groupId);
        URI uri = UriComponent.createUri(STUDY_GROUP_DEFAULT_URI, groupId);
        //그룹아이디만 던져주기
        return ResponseEntity.created(uri).build();
    }

    @DeleteMapping("{study-group-id}/members")
    //그룹 탈퇴
    // 그룹 멤버 탈퇴 시 -> 그룹장이라면 그 다음 가입 멤버가 그룹장이 될 수 있도록 처리
    public ResponseEntity<?> leaveGroup(@AuthenticationPrincipal CustomUserDetails userDetails,
                                        @PathVariable("study-group-id") @Positive Long groupId) {
        studyGroupService.leaveGroup(userDetails.getId(), groupId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("{study-group-id}/members")
    //그룹 내 모든 멤버들
    public ResponseEntity<?> getGroupMembers(@PathVariable("study-group-id") @Positive Long groupId) {
        List<StudyMemberDto.Response> allMembers = studyGroupService.getAllMembers(groupId);
        return new ResponseEntity<>(new ResponseDto.SingleResponseDto<>(allMembers), HttpStatus.OK);
    }

    //그룹장 변경(그룹장일 때만 가능)
    @PatchMapping("{study-group-id}/leader/{member-id}")
    public ResponseEntity<?> changeGroupLeader(@AuthenticationPrincipal CustomUserDetails userDetails,
                                               @PathVariable("study-group-id") @Positive Long groupId,
                                               @PathVariable("member-id") @Positive Long memberId) {
        studyGroupService.giveLeader(userDetails.getId(), groupId, memberId);
        return ResponseEntity.ok().build();
    }

//    @GetMapping("/search")
//    // 그룹명 검색 => 향후 QueryDsl로 처리해보기
//    public ResponseEntity<?> searchStudyGroupByName(@RequestParam("name") String name,
//                                                    @PageableDefault(size = 10) Pageable pageable) {
//        Page<StudyGroupResponseDto> groups = studyGroupService.searchGroupByName(name, pageable);
//        List<StudyGroupResponseDto> content = groups.getContent();
//
//        return new ResponseEntity<>(
//                new ResponseDto.MultipleResponseDto<>(content, groups), HttpStatus.OK);
//    }
}
