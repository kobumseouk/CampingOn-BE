package site.campingon.campingon.user.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.campingon.campingon.user.dto.UserDeactivateRequestDto;
import site.campingon.campingon.user.dto.UserResponseDto;
import site.campingon.campingon.user.dto.UserSignUpRequestDto;
import site.campingon.campingon.user.dto.UserSignUpResponseDto;
import site.campingon.campingon.user.dto.UserUpdateRequestDto;
import site.campingon.campingon.user.entity.Role;
import site.campingon.campingon.user.entity.User;
import site.campingon.campingon.user.mapper.UserMapper;
import site.campingon.campingon.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    private static final String NICKNAME_REGEX = "^[a-zA-Z0-9가-힣]+$";
    private static final String EMAIL_REGEX = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";

    // 회원 가입
    @Transactional
    public UserSignUpResponseDto registerUser(UserSignUpRequestDto userSignUpRequestDto) {
        validateUserEmail(userSignUpRequestDto.getEmail());
        validateUserNickname(userSignUpRequestDto.getNickname());

        String encodedPassword = passwordEncoder.encode(userSignUpRequestDto.getPassword());

        User newUser = User.builder()
            .email(userSignUpRequestDto.getEmail())
            .nickname(userSignUpRequestDto.getNickname())
            .password(encodedPassword)
            .name(userSignUpRequestDto.getName())
            .role(Role.USER)
            .build();

        User user = userRepository.save(newUser);

        log.info("회원 가입 - 이메일: {}", user.getEmail());
        return userMapper.toSignUpResponseDto(user);
    }

    // 회원 정보 조회
    @Transactional(readOnly = true)
    public UserResponseDto getMyInfo(Long userId) {
        User user = userRepository.findByIdAndIsDeletedFalse(userId)
            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        return userMapper.toResponseDto(user);
    }


    // 회원 정보 수정(닉네임, 비밀번호)
    @Transactional
    public UserResponseDto updateUser(Long userId, UserUpdateRequestDto requestDto) {
        // 사용자 정보 조회
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 닉네임 변경
        if (requestDto.getNickname() != null && !requestDto.getNickname().isBlank()) {
            if (userRepository.existsByNicknameAndIsDeletedFalse(requestDto.getNickname())) {
                throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
            }
            user.updateNickname(requestDto.getNickname());
        }

        // 비밀번호 변경
        if (requestDto.getNewPassword() != null && !requestDto.getNewPassword().isBlank()) {
            if (!passwordEncoder.matches(requestDto.getCurrentPassword(), user.getPassword())) {
                throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
            }
            String encodedNewPassword = passwordEncoder.encode(requestDto.getNewPassword());
            user.updatePassword(encodedNewPassword);
        }

        // 변경된 사용자 정보 저장
        User updatedUser = userRepository.save(user);
        log.info("회원 정보 업데이트 - 이메일: {}", updatedUser.getEmail());
        return userMapper.toResponseDto(updatedUser);
    }

    // 회원 탈퇴
    @Transactional
    public void deleteUser(UserDeactivateRequestDto userDeactiveRequestDto) {
        // 사용자 정보 조회
        User user = userRepository.findById(userDeactiveRequestDto.getId())
            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 사용자 탈퇴 처리 (소프트 삭제)
        user.deleteUser(userDeactiveRequestDto.getDeleteReason());
        log.info("회원 탈퇴 - 이메일: {} , 탈퇴 사유: {}", userDeactiveRequestDto.getId(), userDeactiveRequestDto.getDeleteReason());

    }


    // 이메일 validation 메서드
    public void validateUserEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("이메일은 공백일 수 없습니다.");
        }

        if (!email.matches(EMAIL_REGEX)) {
            throw new IllegalArgumentException("유효하지 않은 이메일 형식입니다.");
        }

        if (userRepository.existsByEmailAndIsDeletedFalse(email)) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
        }
    }

    // 닉네임 validation 메서드
    public void validateUserNickname(String nickname) {
        if (nickname == null || nickname.trim().isEmpty()) {
            throw new IllegalArgumentException("닉네임은 공백일 수 없습니다.");
        } else if (nickname.trim().length() > 8) {
            throw new IllegalArgumentException("닉네임은 8자 이내여야 합니다.");
        }
        if (!nickname.matches(NICKNAME_REGEX)) {
            throw new IllegalArgumentException("닉네임은 알파벳, 숫자, 한글만 포함할 수 있습니다.");
        }
        if (userRepository.existsByNicknameAndIsDeletedFalse(nickname)) {
            throw new IllegalArgumentException("이미 존재하는 닉네임입니다.");
        }
    }
}
