package com.group_platform.sutdygroup.controller;

import com.group_platform.response.ResponseDto;
import com.group_platform.security.dto.CustomUserDetails;
import com.group_platform.sutdygroup.dto.StudyGroupDto;
import com.group_platform.sutdygroup.dto.StudyGroupResponseDto;
import com.group_platform.sutdygroup.service.StudyGroupService;
import com.group_platform.util.UriComponent;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

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


    //GET /study-group : 스터디 그룹 목록(메인페이지용)


//    POST /studygroups/{id}/members : 그룹 멤버 가입
//
//    DELETE /studygroups/{id}/members/{userId} : 그룹 멤버 탈퇴 (soft-delete)
    // 그룹 멤버 탈퇴 시 -> 그룹장이라면 그 다음 가입 멤버가 그룹장이 될 수 있도록 처리
//
//    GET /studygroups/{id}/members : 해당 그룹의 멤버 목록

    //그룹장 변경(그룹장일 때만 가능)
}
