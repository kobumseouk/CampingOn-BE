package site.campingon.campingon.user.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;
import site.campingon.campingon.user.dto.UserDeactivateRequestDto;
import site.campingon.campingon.user.dto.UserResponseDto;
import site.campingon.campingon.user.dto.UserSignUpRequestDto;
import site.campingon.campingon.user.dto.UserSignUpResponseDto;
import site.campingon.campingon.user.dto.UserUpdateRequestDto;
import site.campingon.campingon.user.entity.Role;
import site.campingon.campingon.user.entity.User;
import site.campingon.campingon.user.mapper.UserMapper;
import site.campingon.campingon.user.repository.UserRepository;

class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserMapper userMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @DisplayName("TEST - 회원 가입: 정상 동작")
    @Test
    void testRegisterUser_success() {
        // Given
        UserSignUpRequestDto requestDto = new UserSignUpRequestDto("test@example.com", "nickname", "password", "Test User");
        User newUser = User.builder()
            .email(requestDto.getEmail())
            .nickname(requestDto.getNickname())
            .password("encodedPassword")
            .name(requestDto.getName())
            .role(Role.USER)
            .build();
        User savedUser = newUser.toBuilder().id(1L).build();

        when(userRepository.existsByEmailAndIsDeletedFalse(requestDto.getEmail())).thenReturn(false);
        when(userRepository.existsByNicknameAndIsDeletedFalse(requestDto.getNickname())).thenReturn(false);
        when(passwordEncoder.encode(requestDto.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(userMapper.toSignUpResponseDto(any(User.class))).thenReturn(
            UserSignUpResponseDto.builder()
                .id(savedUser.getId())
                .email(savedUser.getEmail())
                .build()
        );

        // When
        UserSignUpResponseDto responseDto = userService.registerUser(requestDto);

        // Then
        assertNotNull(responseDto);
        assertEquals(savedUser.getId(), responseDto.getId());
        assertEquals(savedUser.getEmail(), responseDto.getEmail());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @DisplayName("TEST - 회원 가입 실패: 이메일 중복")
    @Test
    void testRegisterUser_emailDuplicated() {
        // Given
        UserSignUpRequestDto requestDto = new UserSignUpRequestDto("test@example.com", "nickname", "password", "Test User");

        when(userRepository.existsByEmailAndIsDeletedFalse(requestDto.getEmail())).thenReturn(true);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> userService.registerUser(requestDto));
        verify(userRepository, times(0)).save(any(User.class)); // save가 호출되지 않아야 함
    }

    @DisplayName("TEST - 회원 가입 실패: 닉네임 중복")
    @Test
    void testRegisterUser_nicknameDuplicated() {
        // Given
        UserSignUpRequestDto requestDto = new UserSignUpRequestDto("test@example.com", "nickname", "password", "Test User");

        when(userRepository.existsByEmailAndIsDeletedFalse(requestDto.getEmail())).thenReturn(false);
        when(userRepository.existsByNicknameAndIsDeletedFalse(requestDto.getNickname())).thenReturn(true);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> userService.registerUser(requestDto));
        verify(userRepository, times(0)).save(any(User.class)); // save가 호출되지 않아야 함
    }

    @DisplayName("TEST - 회원 가입 실패: 필수 입력값 누락")
    @Test
    void testRegisterUser_requiredFieldsMissing() {
        // Given
        UserSignUpRequestDto requestDto = new UserSignUpRequestDto(null, "nickname", "password", "Test User");

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> userService.registerUser(requestDto));
        verify(userRepository, times(0)).save(any(User.class)); // save가 호출되지 않아야 함
    }

    @DisplayName("TEST - 회원정보 확인: 정상 동작")
    @Test
    void testCheckInfo_success() {
        // Given
        Long userId = 1L;
        User user = User.builder().id(userId).email("test@example.com").nickname("nickname").build();
        when(userRepository.findByIdAndIsDeletedFalse(userId)).thenReturn(Optional.of(user));
        when(userMapper.toResponseDto(user)).thenReturn(
            UserResponseDto.builder()
                .id(userId)
                .email(user.getEmail())
                .nickname(user.getNickname())
                .build()
        );

        // When
        UserResponseDto responseDto = userService.getMyInfo(userId);

        // Then
        assertNotNull(responseDto);
        assertEquals(userId, responseDto.getId());
        verify(userRepository, times(1)).findByIdAndIsDeletedFalse(userId);
    }

    @DisplayName("TEST - 회원정보 수정: 정상 동작")
    @Test
    void updateUser_success() {
        // Given
        Long userId = 1L;
        User user = User.builder().id(userId).email("test@example.com").nickname("oldNickname").password("encodedPassword").build();
        UserUpdateRequestDto requestDto = UserUpdateRequestDto.builder()
            .nickname("newNickname")
            .currentPassword("currentPassword")
            .newPassword("newPassword")
            .build();

        // Mock
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.existsByNicknameAndIsDeletedFalse(requestDto.getNickname())).thenReturn(false);
        when(passwordEncoder.matches("currentPassword", user.getPassword())).thenReturn(true);
        when(passwordEncoder.encode("newPassword")).thenReturn("newEncodedPassword");
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toResponseDto(user)).thenReturn(UserResponseDto.builder()
            .id(userId)
            .email(user.getEmail())
            .nickname(requestDto.getNickname())
            .build());

        // When
        UserResponseDto responseDto = userService.updateUser(userId, requestDto);

        // Then
        assertNotNull(responseDto);
        assertEquals(requestDto.getNickname(), responseDto.getNickname());
        verify(userRepository, times(1)).save(user);
    }

    @DisplayName("TEST - 회원 탈퇴: 정상 동작")
    @Test
    void deleteUser_success() {
        // Given
        Long userId = 1L;
        UserDeactivateRequestDto requestDto = UserDeactivateRequestDto.builder()
            .id(userId)
            .deleteReason("탈퇴합니다.")
            .build();
        User user = User.builder().id(userId).email("test@example.com").nickname("nickname").build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // When
        userService.deleteUser(requestDto);

        // Then
        assertTrue(user.isDeleted());
        assertEquals(requestDto.getDeleteReason(), user.getDeleteReason());
        verify(userRepository, times(1)).findById(userId);
    }

    @DisplayName("TEST - 이메일 검증 테스트")
    @Test
    void testValidateUserEmail() {
        // null 또는 공백
        assertThrows(IllegalArgumentException.class,
            () -> userService.validateUserEmail(null),
            "이메일은 공백일 수 없습니다.");

        // 유효하지 않은 형식
        assertThrows(IllegalArgumentException.class,
            () -> userService.validateUserEmail("invalidEmail"),
            "유효하지 않은 이메일 형식입니다.");

        // 이미 존재하는 이메일
        when(userRepository.existsByEmailAndIsDeletedFalse("test@example.com")).thenReturn(true);
        assertThrows(IllegalArgumentException.class,
            () -> userService.validateUserEmail("test@example.com"),
            "이미 존재하는 이메일입니다.");

        // 유효한 이메일
        when(userRepository.existsByEmailAndIsDeletedFalse("test@example.com")).thenReturn(false);
        assertDoesNotThrow(() -> userService.validateUserEmail("test@example.com"));
    }

    @DisplayName("TEST - 닉네임 검증 테스트")
    @Test
    void testValidateUserNickname() {
        // null 또는 공백
        assertThrows(IllegalArgumentException.class,
            () -> userService.validateUserNickname(null),
            "닉네임은 공백일 수 없습니다.");

        // 길이 초과
        assertThrows(IllegalArgumentException.class,
            () -> userService.validateUserNickname("InvalidNickname"),
            "닉네임은 8자 이내여야 합니다.");

        // 유효하지 않은 문자 포함
        assertThrows(IllegalArgumentException.class,
            () -> userService.validateUserNickname("!@#$%^&"),
            "닉네임은 알파벳, 숫자, 한글만 포함할 수 있습니다.");

        // 이미 존재하는 닉네임
        when(userRepository.existsByNicknameAndIsDeletedFalse("nickname")).thenReturn(true);
        assertThrows(IllegalArgumentException.class,
            () -> userService.validateUserNickname("nickname"),
            "이미 존재하는 닉네임입니다.");

        // 유효한 닉네임
        when(userRepository.existsByNicknameAndIsDeletedFalse("캠핑온")).thenReturn(false);
        assertDoesNotThrow(() -> userService.validateUserNickname("캠핑온"));
    }



}