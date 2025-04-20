package org.example.expert.domain.auth.service;

import org.example.expert.config.JwtUtil;
import org.example.expert.config.PasswordEncoder;
import org.example.expert.domain.auth.dto.request.SigninRequest;
import org.example.expert.domain.auth.dto.request.SignupRequest;
import org.example.expert.domain.auth.dto.response.SigninResponse;
import org.example.expert.domain.auth.dto.response.SignupResponse;
import org.example.expert.domain.auth.exception.AuthException;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtUtil jwtUtil;
    @InjectMocks
    private AuthService authService;

    @Test
    void signup_이미_존재하는_이메일() {
        // given
        SignupRequest signupRequest = new SignupRequest("test@example.com", "password", "USER");
        when(userRepository.existsByEmail(signupRequest.getEmail())).thenReturn(true);

        // when
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> authService.signup(signupRequest));

        // then
        assertEquals("이미 존재하는 이메일입니다.", exception.getMessage());
    }

    @Test
    void signup_정상_회원가입() {
        // given
        SignupRequest signupRequest = new SignupRequest("test@example.com", "password", "USER");
        when(userRepository.existsByEmail(signupRequest.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(signupRequest.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(new User("test@example.com", "encodedPassword", UserRole.USER));
        when(jwtUtil.createToken(any(), anyString(), any())).thenReturn("testToken");

        // when
        SignupResponse signupResponse = authService.signup(signupRequest);

        // then
        assertEquals("testToken", signupResponse.getBearerToken());
    }

    @Test
    void signin_가입되지_않은_유저() {
        // given
        SigninRequest signinRequest = new SigninRequest("test@example.com", "password");
        when(userRepository.findByEmail(signinRequest.getEmail())).thenReturn(Optional.empty());

        // when
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> authService.signin(signinRequest));

        // then
        assertEquals("가입되지 않은 유저입니다.", exception.getMessage());
    }

    @Test
    void signin_잘못된_비밀번호() {
        // given
        SigninRequest signinRequest = new SigninRequest("test@example.com", "wrongPassword");
        User user = new User("test@example.com", "encodedPassword", UserRole.USER);
        when(userRepository.findByEmail(signinRequest.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(signinRequest.getPassword(), user.getPassword())).thenReturn(false);

        // when
        AuthException exception = assertThrows(AuthException.class, () -> authService.signin(signinRequest));

        // then
        assertEquals("잘못된 비밀번호입니다.", exception.getMessage());
    }

    @Test
    void signin_정상_로그인() {
        // given
        SigninRequest signinRequest = new SigninRequest("test@example.com", "password");
        User user = new User("test@example.com", "encodedPassword", UserRole.USER);
        when(userRepository.findByEmail(signinRequest.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(signinRequest.getPassword(), user.getPassword())).thenReturn(true);
        when(jwtUtil.createToken(any(), anyString(), any())).thenReturn("testToken");

        // when
        SigninResponse signinResponse = authService.signin(signinRequest);

        // then
        assertEquals("testToken", signinResponse.getBearerToken());
    }
}
