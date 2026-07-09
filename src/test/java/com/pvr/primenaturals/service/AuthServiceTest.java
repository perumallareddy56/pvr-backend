package com.pvr.primenaturals.service;

import com.pvr.primenaturals.dto.request.LoginRequest;
import com.pvr.primenaturals.dto.request.ResetPasswordRequest;
import com.pvr.primenaturals.dto.request.SignupRequest;
import com.pvr.primenaturals.dto.response.JwtResponse;
import com.pvr.primenaturals.entity.User;
import com.pvr.primenaturals.repository.UserRepository;
import com.pvr.primenaturals.security.JwtUtils;
import com.pvr.primenaturals.security.UserDetailsImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder encoder;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private EmailService emailService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @InjectMocks
    private AuthService authService;

    @Test
    public void testAuthenticateUser_Success() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password");

        Authentication authentication = mock(Authentication.class);
        // UserDetailsImpl(Long id, String name, String email, String password, Collection authorities)
        UserDetailsImpl userDetails = new UserDetailsImpl(1L, "Test User", "test@example.com", "password", Collections.emptyList());

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(jwtUtils.generateTokenFromUsername("test@example.com")).thenReturn("mock-jwt-token");

        JwtResponse response = authService.authenticateUser(loginRequest);

        assertNotNull(response);
        assertEquals("mock-jwt-token", response.getToken());
        assertEquals("test@example.com", response.getEmail());
    }

    @Test
    public void testRegisterUser_Success() {
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setName("New User");
        signupRequest.setEmail("new@example.com");
        signupRequest.setPassword("password");

        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(encoder.encode("password")).thenReturn("encoded-password");

        assertDoesNotThrow(() -> authService.registerUser(signupRequest));

        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    public void testRegisterUser_ThrowsExceptionWhenEmailExists() {
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setEmail("existing@example.com");

        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> authService.registerUser(signupRequest));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    public void testProcessResetPassword_ThrowsExceptionWhenTokenInvalid() {
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setToken("invalid-token");
        request.setNewPassword("new-password");

        when(userRepository.findByResetToken("invalid-token")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> authService.processResetPassword(request));
    }
}
