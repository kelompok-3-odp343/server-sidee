package com.example.wandoor.service;

import com.example.wandoor.exception.BusinessException;
import com.example.wandoor.model.entity.OtpVerification;
import com.example.wandoor.model.entity.Profile;
import com.example.wandoor.model.entity.RoleManagement;
import com.example.wandoor.model.entity.UserAuth;
import com.example.wandoor.model.request.LoginRequest;
import com.example.wandoor.model.request.VerifyOtpRequest;
import com.example.wandoor.repository.*;
import com.example.wandoor.util.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoginOtpServiceTest {

    @Mock
    private UserAuthRepository userAuthRepository;
    @Mock
    private UserOtpVerificationRepository userOtpVerificationRepository;
    @Mock
    private ProfileRepository profileRepository;
    @Mock
    private RoleManagementRepository roleManagementRepository;
    @Mock
    private EmailService emailService;
    @Mock
    private JwtUtils jwtUtils;
    @Mock
    private StringRedisTemplate stringRedisTemplate;
    @Mock
    private HashOperations<String, String, String> hashOperations;
    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private LoginOtpService loginOtpService;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @BeforeEach
    void setUp() {
        lenient().when(stringRedisTemplate.opsForHash()).thenReturn((HashOperations) hashOperations);
        lenient().when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void testLogin_success() {
        var req = new LoginRequest("oktaviaqa", "123456");

        var userAuth = new UserAuth();
        userAuth.setUserId("U001");
        userAuth.setUsername("oktaviaqa");
        userAuth.setPassword(encoder.encode("123456"));
        userAuth.setEmailAddress("oktaviaqa@example.com");
        userAuth.setIsUserBlocked(0);

        when(userAuthRepository.findByUsername("oktaviaqa")).thenReturn(Optional.of(userAuth));

        var profile = new Profile();
        profile.setId("U001");
        profile.setEmailAddress("oktaviaqa@example.com");
        lenient().when(profileRepository.findById("U001")).thenReturn(Optional.of(profile));

        var role = new RoleManagement();
        role.setId("R001");
        role.setRoleName("NASABAH");
        lenient().when(roleManagementRepository.findById(any()))
                .thenReturn(Optional.of(role));

        lenient().when(stringRedisTemplate.hasKey(anyString())).thenReturn(false);
        lenient().when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        lenient().when(stringRedisTemplate.opsForHash()).thenReturn((HashOperations) hashOperations);

        doNothing().when(hashOperations).putAll(anyString(), anyMap());

        doNothing().when(emailService).sendOtp(anyString(), anyString());

        var response = loginOtpService.login(req);

        assertThat(response.status()).isTrue();
        assertThat(response.message()).contains("Kode OTP");
        assertThat(response.sessionIdOrToken()).isNotNull();

        verify(emailService, times(1)).sendOtp(eq("oktaviaqa@example.com"), anyString());
    }

    @Test
    void testLogin_wrongPassword_shouldThrow() {
        var req = new LoginRequest("oktaviaqa", "wrongpass");

        var userAuth = new UserAuth();
        userAuth.setUserId("U001");
        userAuth.setUsername("oktaviaqa");
        userAuth.setPassword(encoder.encode("123456"));
        userAuth.setIsUserBlocked(0);

        lenient().when(userAuthRepository.findByUsername("oktaviaqa")).thenReturn(Optional.of(userAuth));

        lenient().when(stringRedisTemplate.hasKey(anyString())).thenReturn(false);
        lenient().when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        lenient().when(valueOperations.increment(anyString())).thenReturn(1L);
        lenient().when(stringRedisTemplate.expire(anyString(), any())).thenReturn(true);

        var ex = catchThrowable(() -> loginOtpService.login(req));

        assertThat(ex)
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Username atau Password salah");
    }

    @Test
    void testLogin_blockedUser_shouldThrow() {
        var req = new LoginRequest("oktaviaqa", "123456");

        var userAuth = new UserAuth();
        userAuth.setUserId("U001");
        userAuth.setUsername("oktaviaqa");
        userAuth.setPassword(encoder.encode("123456"));
        userAuth.setIsUserBlocked(1);

        when(userAuthRepository.findByUsername("oktaviaqa")).thenReturn(Optional.of(userAuth));

        var ex = catchThrowable(() -> loginOtpService.login(req));

        assertThat(ex)
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Akun diblokir");
    }

    @Test
    void testVerifyOtp_success() {
        var otpId = UUID.randomUUID().toString();
        var req = new VerifyOtpRequest(otpId, "654321");

        var role = new RoleManagement();
        role.setId("R001");
        role.setRoleName("NASABAH");

        var otpVerification = OtpVerification.builder()
                .id(otpId)
                .otpCode("654321")
                .userId("U001")
                .isUsed(0)
                .expiresAt(LocalDateTime.now().plusMinutes(2))
                .build();
        var profile = new Profile();
        profile.setId("U001");
        profile.setEmailAddress("oktaviaqa@example.com");

        String otpSessionKey = "otp:session:" + otpId;
        Map<String, String> mockOtpData = Map.of(
                "otp", "654321",
                "username", "oktaviaqa",
                "userId", "U001"
        );

        lenient().when(userOtpVerificationRepository.findById(anyString())).thenReturn(Optional.of(otpVerification));
        lenient().when(userOtpVerificationRepository.consumeIfValid(anyString(), anyString(), any())).thenReturn(1);
        lenient().when(userAuthRepository.findById("U001"))
                .thenReturn(Optional.of(new UserAuth("U001", "oktaviaqa", null, null, null, 0)));
        lenient().when(roleManagementRepository.findById(any()))
                .thenReturn(Optional.of(role));

        lenient().when(profileRepository.findById("U001")).thenReturn(Optional.of(profile));
        lenient().when(jwtUtils.generateToken(anyMap(), anyString())).thenReturn("jwt_token");

        lenient().when(stringRedisTemplate.opsForHash()).thenReturn((HashOperations) hashOperations);
        lenient().when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        lenient().when(hashOperations.entries(otpSessionKey)).thenReturn(mockOtpData);

        var response = loginOtpService.verifyOtp(req);

        assertThat(response.status()).isTrue();
        assertThat(response.message()).isEqualTo("login berhasil");
        assertThat(response.token()).isEqualTo("jwt_token");
    }


    @Test
    void testVerifyOtp_expired_shouldThrow() {
        var otpId = UUID.randomUUID().toString();
        var req = new VerifyOtpRequest(otpId.toString(), "123456");

        String otpSessionKey = "otp:session:" + otpId;

        lenient().when(hashOperations.entries(eq(otpSessionKey))).thenReturn(Map.of());

        lenient().when(userAuthRepository.findById(eq("U001"))).thenReturn(Optional.of(new UserAuth("U001", "oktaviaqa", null, null, null, 0)));

        var ex = catchThrowable(() -> loginOtpService.verifyOtp(req));

        assertThat(ex)
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("OTP expired atau tidak ditemukan");

    }
}