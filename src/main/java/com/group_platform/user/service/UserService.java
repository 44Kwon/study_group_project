package com.group_platform.user.service;

import com.group_platform.exception.BusinessLogicException;
import com.group_platform.exception.ExceptionCode;
import com.group_platform.user.dto.UserDto;
import com.group_platform.user.dto.UserResponseDto;
import com.group_platform.user.entity.User;
import com.group_platform.user.mapper.UserMapper;
import com.group_platform.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

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

        //비밀번호 암호화 필요(나중에)

        User savedUser = userRepository.save(createUser);

        return savedUser.getId();
    }


    //회원변경(보통 회원 정보 다 던져줘야 하나)
    public UserDto.updateResponse updateUser(UserDto.UpdateRequest updateRequest) {
        User updateUser = userMapper.UpdateRequestToUser(updateRequest);
        //유저가 존재하는지 검증
        User user = validateUser(updateUser.getUsername());

        //넘어온 값들에 대한 검증
        //닉네임 검증
        if(updateUser.getNickname() != null && !updateUser.getNickname().equals(user.getNickname())) {
            validateNickname(updateUser.getNickname());
        }

        //이메일 검증
        if(updateUser.getEmail() != null && user.getEmail() == null) {
            validateEmail(updateUser.getEmail());
        } else {
            //이메일 수정 시 에러발생
            throw new BusinessLogicException(ExceptionCode.USER_EMAIL_CANNOT_UPDATE);
        }

        //수정로직
        Optional.ofNullable(updateUser.getNickname())
                .ifPresent(user::changeNickname);

        Optional.ofNullable(updateUser.getEmail())
                .ifPresent((email) -> user.changeEmail(email));

        //수정 시간이 만약 이상이 있다면 직접 값을 넣어 보내주거나 flush 해줘야 한다.

        return userMapper.UserToUpdateResponse(user);
    }

    //비밀번호 변경
    public void updatePassword(Long userId, UserDto.UpdatePasswordRequest updatePasswordRequest) {
        User user = validateUserWithUserId(userId);

        // 나중에 변경해야 함(Encoder 도입 후)
        if (user.getPassword() != updatePasswordRequest.getCurrentPw()) {
            throw new BusinessLogicException(ExceptionCode.PASSWORD_MISMATCH);
        }

        if (updatePasswordRequest.getNewPw() == null || updatePasswordRequest.getNewPw().isBlank()) {
            throw new IllegalArgumentException("새로운 비밀번호를 입력해주세요");
        }
        if (updatePasswordRequest.getNewPw().equals(user.getPassword())) {
            throw new BusinessLogicException(ExceptionCode.PASSWORD_SAME_AS_OLD);
        }

        user.changePassword(updatePasswordRequest.getNewPw());
    }

    //회원 조회(내 페이지)
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
        List<User> users = userRepository.findExpiredWithdrawnUsers(cutoff);
        userRepository.deleteAll(users);
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

    //아이디로 회원이 존재하는지(나중에 로그인 구현 시 변경 소지 있음)
    private User validateUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(()-> new BusinessLogicException(ExceptionCode.USER_NOT_FOUND));
    }

    //회원키로 회원이 존재하는지
    private User validateUserWithUserId(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessLogicException(ExceptionCode.USER_NOT_FOUND));
    }
}
