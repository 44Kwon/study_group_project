package com.group_platform.user.service;

import com.group_platform.exception.BusinessLogicException;
import com.group_platform.exception.ExceptionCode;
import com.group_platform.post.bookmark.PostBookmark;
import com.group_platform.post.dto.PostDto;
import com.group_platform.post.mapper.PostBookmarkMapper;
import com.group_platform.post.repository.PostBookmarkRepository;
import com.group_platform.response.PageInfo;
import com.group_platform.user.dto.UserDto;
import com.group_platform.user.dto.UserMyProfileDto;
import com.group_platform.user.dto.UserResponseDto;
import com.group_platform.user.entity.User;
import com.group_platform.user.mapper.UserMapper;
import com.group_platform.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final PostBookmarkRepository postBookmarkRepository;
    private final PostBookmarkMapper postBookmarkMapper;

    //회원가입
    public Long creatUser(UserDto.CreateRequest createRequest) {
        User createUser = userMapper.CreateRequestToUser(createRequest);
        //아이디 중복 검증
        validateUsername(createUser.getUsername());

        //닉네임 중복 검증
        validateNickname(createUser.getNickname());

        //회원가입 시 이메일도 적으면 이메일 중복 검증
        //향후 이메일 관련 추가적 검증 처리 필요
        if (createUser.getEmail() != null && !createUser.getEmail().isEmpty()) {
            validateEmail(createUser.getEmail());
        }

        //비밀번호 암호화
        createUser.updateEncodingPassword(passwordEncoder.encode(createUser.getPassword()));


        User savedUser = userRepository.save(createUser);

        return savedUser.getId();
    }


    //회원변경(보통 회원 정보 다 던져줘야 하나)
    public UserDto.UpdateResponse updateUser(Long userId, UserDto.UpdateRequest updateRequest) {
        User updateUser = userMapper.UpdateRequestToUser(updateRequest);
        //유저가 존재하는지 검증
        User user = validateUserWithUserId(userId);

        //넘어온 값들에 대한 검증
        //닉네임 변경검증
        validateNicknameUpdate(updateUser, user);

        //이메일 변경검증
        validateEmailUpdate(updateUser, user);

        //수정로직
        Optional.ofNullable(updateUser.getNickname())
                .ifPresent(user::changeNickname);

        Optional.ofNullable(updateUser.getEmail())
                .ifPresent((email) -> user.changeEmail(email));

        //수정 시간이 만약 이상이 있다면 직접 값을 넣어 보내주거나 flush 해줘야 한다.

        return userMapper.UserToUpdateResponse(user);
    }

    private void validateEmailUpdate(User updateUser, User user) {
        // 현재 프로젝트에서 Email 업데이트 검증은
        // 초기 회원가입 시 Email을 작성하지 않은 사람에 한해서만 업데이트 가능하도록 했다.

        //guard clause 패턴
        if (updateUser.getEmail() == null) {
            return; // 변경 요청 없음, 그냥 종료
        }
        if (updateUser.getEmail().equals(user.getEmail())) {
            throw new BusinessLogicException(ExceptionCode.SAME_EMAIL);
        }
        if (user.getEmail() == null) {
            validateEmail(updateUser.getEmail());
            return;
        }

        throw new BusinessLogicException(ExceptionCode.USER_EMAIL_CANNOT_UPDATE);
    }

    private void validateNicknameUpdate(User updateUser, User user) {
        if(updateUser.getNickname() != null && updateUser.getNickname().equals(user.getNickname())) {
            throw new BusinessLogicException(ExceptionCode.SAME_NICKNAME);
        } else if (updateUser.getNickname() != null) {
            validateNickname(updateUser.getNickname());
        }
    }

    //비밀번호 변경
    public void updatePassword(Long userId, UserDto.UpdatePasswordRequest updatePasswordRequest) {
        User user = validateUserWithUserId(userId);

        if (!passwordEncoder.matches(updatePasswordRequest.getCurrentPw(),user.getPassword())) {
            throw new BusinessLogicException(ExceptionCode.PASSWORD_MISMATCH);
        }
        if (updatePasswordRequest.getNewPw() == null || updatePasswordRequest.getNewPw().isBlank()) {
            throw new IllegalArgumentException("새로운 비밀번호를 입력해주세요");
        }
        if (passwordEncoder.matches(updatePasswordRequest.getNewPw(),user.getPassword())) {
            throw new BusinessLogicException(ExceptionCode.PASSWORD_SAME_AS_OLD);
        }

        user.changePassword(passwordEncoder.encode(updatePasswordRequest.getNewPw()));
    }

    //회원 조회(내 페이지)
    //찜 조회 추가
    @Transactional(readOnly = true)
    public UserMyProfileDto getMyUser(Long userId) {
        User user = validateUserWithUserId(userId);
        //찜 조회
        Page<PostBookmark> postMyBookmark = postBookmarkRepository.findPostMyBookmark(userId, PageRequest.of(0, 10));
        List<PostDto.PostBookMarkResponse> postBookMarkResponses = postBookmarkMapper.postBookmarksToPostBookmarkResponse(postMyBookmark.getContent());

        UserMyProfileDto userMyProfileDto = userMapper.userToUserMyProfileDto(user);
        userMyProfileDto.setBookmarks(postBookMarkResponses);
        userMyProfileDto.setBookmarksPage(
                new PageInfo(postMyBookmark.getNumber()+1, postMyBookmark.getSize(), postMyBookmark.getTotalElements(), postMyBookmark.getTotalPages()));
        return userMyProfileDto;
    }

    //찜 페이지네이션
    @Transactional(readOnly = true)
    public Page<PostDto.PostBookMarkResponse> getMyPostBookmarks(Long userId, Pageable pageable) {
        User user = validateUserWithUserId(userId);
        Page<PostBookmark> postMyBookmark = postBookmarkRepository.findPostMyBookmark(userId, pageable);
        List<PostDto.PostBookMarkResponse> postBookMarkResponses = postBookmarkMapper.postBookmarksToPostBookmarkResponse(postMyBookmark.getContent());
        return new PageImpl<>(postBookMarkResponses, pageable, postMyBookmark.getTotalElements());
    }


    //회원 조회(다른 사용자 조회)
    @Transactional(readOnly = true)
    public UserResponseDto getUser(Long userId) {
        User user = validateUserWithUserId(userId);
        return userMapper.UserToUserResponseDto(user);
    }

    //회원탈퇴(status 변경)
    public void deleteUser(Long userId) {
        User user = validateUserWithUserId(userId);
        user.withdraw(user.getUserStatus()); //유저의 현재 상태가 활성화이면 탈퇴 상태로 변경
    }

    //회원삭제(스케줄러로 삭제)
    //만약 성능문제가 생긴다면 bulk delete JPQL 사용해야함 => 그러면 casacde 동작안함
    public void deleteExpiredUsers() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(30);
        log.info("[UserCleanupScheduler] 탈퇴 후 30일 지난 유저 삭제 작업 시작. 기준 시각: {}", cutoff);
        List<User> users = userRepository.findExpiredWithdrawnUsers(cutoff);
        if (users.isEmpty()) {
            log.info("[UserCleanupScheduler] 삭제할 유저 없음. 작업 종료.");
            return;
        }

        log.info("[UserCleanupScheduler] 삭제 대상 유저 수: {}", users.size());
        for (User user : users) {
            log.info("[UserCleanupScheduler] 삭제 대상 유저 ID: {}, 이메일: {}", user.getId(), user.getEmail());
        }
        userRepository.deleteAll(users);
        log.info("[UserCleanupScheduler] 탈퇴 유저 삭제 작업 완료. 총 삭제 수: {}", users.size());
    }


    //회원가입 시 유저아이디 중복검증(존재하는지)
    private void validateUsername(String username) {
        if (userRepository.existsByUsername(username)) {
            throw new BusinessLogicException(ExceptionCode.USER_USERNAME_DUPLICATED);
        }
    }

    //회원가입 시 유저이메일 중복검증(존재하는지)
    private void validateEmail(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new BusinessLogicException(ExceptionCode.USER_EMAIL_DUPLICATED);
        }
    }

    //회원가입 시 유저닉네임 중복검증(존재하는지)
    private void validateNickname(String nickname) {
        if (userRepository.existsByNickname(nickname)) {
            throw new BusinessLogicException(ExceptionCode.USER_NICKNAME_DUPLICATED);
        }
    }

    //회원키로 회원이 존재하는지
    @Transactional(readOnly = true)
    public User validateUserWithUserId(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessLogicException(ExceptionCode.USER_NOT_FOUND));
    }
}
