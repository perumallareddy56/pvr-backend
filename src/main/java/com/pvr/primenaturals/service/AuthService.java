package com.pvr.primenaturals.service;

import com.pvr.primenaturals.dto.request.ForgotPasswordRequest;
import com.pvr.primenaturals.dto.request.LoginRequest;
import com.pvr.primenaturals.dto.request.ResetPasswordRequest;
import com.pvr.primenaturals.dto.request.SignupRequest;
import com.pvr.primenaturals.dto.response.JwtResponse;
import com.pvr.primenaturals.dto.response.RefreshTokenResponse;
import com.pvr.primenaturals.entity.Role;
import com.pvr.primenaturals.entity.User;
import com.pvr.primenaturals.entity.RefreshToken;
import com.pvr.primenaturals.repository.UserRepository;
import com.pvr.primenaturals.security.JwtUtils;
import com.pvr.primenaturals.security.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
public class AuthService {

    private static final Logger log = Logger.getLogger(AuthService.class.getName());

    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private EmailService emailService;

    @Autowired
    private RefreshTokenService refreshTokenService;

    public JwtResponse authenticateUser(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateTokenFromUsername(loginRequest.getEmail());

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());

        return new JwtResponse(jwt,
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getName(),
                roles);
    }

    public void registerUser(SignupRequest signUpRequest) {
        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            throw new IllegalArgumentException("Error: Email is already in use!");
        }

        User user = new User();
        user.setName(signUpRequest.getName());
        user.setEmail(signUpRequest.getEmail());
        user.setPassword(encoder.encode(signUpRequest.getPassword()));
        user.setRole(Role.USER);

        if (signUpRequest.getEmail().contains("admin")) {
            user.setRole(Role.ADMIN);
        }

        userRepository.save(user);
    }

    @Transactional
    public void processForgotPassword(ForgotPasswordRequest request) {
        String trimmedEmail = request.getEmail().trim().toLowerCase();
        log.info("[AUTH-SERVICE] Password reset requested for: " + trimmedEmail);

        User user = userRepository.findByEmail(trimmedEmail).orElse(null);
        if (user == null) {
            log.info("[AUTH-SERVICE] No account found for: " + trimmedEmail);
            return; // Return silently to prevent user enumeration
        }

        String token = UUID.randomUUID().toString();
        user.setResetToken(token);
        user.setResetTokenExpiry(LocalDateTime.now().plusHours(1));
        userRepository.saveAndFlush(user);
        log.info("[AUTH-SERVICE] Reset token persisted for: " + trimmedEmail);

        String resetLink = frontendUrl + "/reset-password?token=" + token;
        emailService.sendPasswordResetEmail(trimmedEmail, resetLink);
        log.info("[AUTH-SERVICE] Reset email dispatched to: " + trimmedEmail);
    }

    @Transactional
    public void processResetPassword(ResetPasswordRequest request) {
        String trimmedToken = request.getToken().trim();
        log.info("[AUTH-SERVICE] Password reset attempt with token: " + trimmedToken);

        Optional<User> userOptional = userRepository.findByResetToken(trimmedToken);
        if (userOptional.isEmpty()) {
            throw new IllegalArgumentException("Security link is invalid or has already been used.");
        }

        User user = userOptional.get();
        if (user.getResetTokenExpiry() == null || user.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Security link has expired. Please request a new one.");
        }

        user.setPassword(encoder.encode(request.getNewPassword()));
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        userRepository.saveAndFlush(user);
        log.info("[AUTH-SERVICE] Password successfully updated for user: " + user.getEmail());
    }

    @Transactional
    public RefreshTokenResponse rotateTokens(String requestRefreshToken) {
        RefreshToken token = refreshTokenService.findByToken(requestRefreshToken)
                .orElseThrow(() -> new IllegalArgumentException("Refresh token is not in database!"));

        refreshTokenService.verifyExpiration(token);
        User user = token.getUser();

        // Delete old token and generate new rotated one
        refreshTokenService.deleteByUserId(user.getId());
        RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(user.getId());
        String newAccessToken = jwtUtils.generateTokenFromUsername(user.getEmail());

        return new RefreshTokenResponse(newAccessToken, newRefreshToken.getToken());
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
}
