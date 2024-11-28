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
import site.campingon.campingon.common.jwt.RefreshTokenService;
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
    private RefreshTokenService refreshTokenService; // MockBean 사용

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
            .role(Role.ROLE_USER)
            .build();

        User savedUser = newUser.toBuilder().id(1L).build();

        // Mocking: 이메일과 닉네임이 중복되지 않은 경우
        when(userRepository.findByEmailOrNicknameAndDeletedAtIsNull(requestDto.getEmail(), requestDto.getNickname()))
            .thenReturn(Optional.empty());

        // Mocking: 비밀번호 인코딩
        when(passwordEncoder.encode(requestDto.getPassword()))
            .thenReturn("encodedPassword");

        // Mocking: 새 사용자 저장
        when(userRepository.save(any(User.class)))
            .thenReturn(savedUser);

        // Mocking: 저장된 사용자 DTO로 매핑
        when(userMapper.toSignUpResponseDto(any(User.class)))
            .thenReturn(UserSignUpResponseDto.builder()
                .id(savedUser.getId())
                .email(savedUser.getEmail())
                .build());

        // When
        UserSignUpResponseDto responseDto = userService.registerUser(requestDto);

        // Then
        assertNotNull(responseDto); // 결과가 null이 아닌지 확인
        assertEquals(savedUser.getId(), responseDto.getId()); // 반환된 ID가 예상값과 일치하는지 확인
        assertEquals(savedUser.getEmail(), responseDto.getEmail()); // 반환된 이메일이 예상값과 일치하는지 확인
        verify(userRepository, times(1)).findByEmailOrNicknameAndDeletedAtIsNull(requestDto.getEmail(), requestDto.getNickname());
        verify(userRepository, times(1)).save(any(User.class));
        verify(passwordEncoder, times(1)).encode(requestDto.getPassword());
        verify(userMapper, times(1)).toSignUpResponseDto(any(User.class));
    }


    @DisplayName("TEST - 회원정보 확인: 정상 동작")
    @Test
    void testCheckInfo_success() {
        // Given
        Long userId = 1L;
        User user = User.builder().id(userId).email("test@example.com").nickname("nickname").build();
        when(userRepository.findByIdAndDeletedAtIsNull(userId)).thenReturn(Optional.of(user));
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
        verify(userRepository, times(1)).findByIdAndDeletedAtIsNull(userId);
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
        when(userRepository.findByIdAndDeletedAtIsNull(userId)).thenReturn(Optional.of(user));
        when(userRepository.existsByNickname(requestDto.getNickname())).thenReturn(false);
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
        String deleteReason = "deleteReason";
        User user = User.builder()
            .id(userId)
            .email("test@example.com")
            .nickname("nickname")
            .deleteReason(deleteReason)
            .build();

        when(userRepository.findByIdAndDeletedAtIsNull(userId)).thenReturn(Optional.of(user)); // 수정됨

        // When
        userService.deleteUser(userId, deleteReason);

        // Then
        assertNotNull(user.getDeletedAt());
        assertEquals(deleteReason, user.getDeleteReason());
        verify(userRepository, times(1)).findByIdAndDeletedAtIsNull(userId); // 수정됨
    }
}