package com.pvr.primenaturals.controller;

import com.pvr.primenaturals.dto.request.LoginRequest;
import com.pvr.primenaturals.dto.request.SignupRequest;
import com.pvr.primenaturals.dto.response.JwtResponse;
import com.pvr.primenaturals.dto.response.MessageResponse;
import com.pvr.primenaturals.dto.request.ForgotPasswordRequest;
import com.pvr.primenaturals.dto.request.ResetPasswordRequest;
import com.pvr.primenaturals.dto.response.RefreshTokenResponse;
import com.pvr.primenaturals.entity.RefreshToken;
import com.pvr.primenaturals.security.JwtUtils;
import com.pvr.primenaturals.service.AuthService;
import com.pvr.primenaturals.service.RefreshTokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.logging.Logger;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger log = Logger.getLogger(AuthController.class.getName());

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        JwtResponse jwtResponse = authService.authenticateUser(loginRequest);

        // Create refresh token in DB
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(jwtResponse.getId());

        // Generate HttpOnly cookies
        ResponseCookie jwtCookie = jwtUtils.generateJwtCookieFromUsername(jwtResponse.getEmail());
        ResponseCookie jwtRefreshCookie = jwtUtils.generateRefreshJwtCookie(refreshToken.getToken());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                .header(HttpHeaders.SET_COOKIE, jwtRefreshCookie.toString())
                .body(jwtResponse);
    }

    @PostMapping("/refreshtoken")
    public ResponseEntity<?> refreshtoken(HttpServletRequest request) {
        String refreshToken = jwtUtils.getJwtRefreshFromCookies(request);

        if (refreshToken != null && !refreshToken.isEmpty()) {
            try {
                RefreshTokenResponse response = authService.rotateTokens(refreshToken);

                RefreshToken dbToken = refreshTokenService.findByToken(response.getRefreshToken())
                        .orElseThrow(() -> new IllegalArgumentException("Rotated token not found in database!"));

                ResponseCookie newJwtCookie = jwtUtils.generateJwtCookieFromUsername(dbToken.getUser().getEmail());
                ResponseCookie newJwtRefreshCookie = jwtUtils.generateRefreshJwtCookie(response.getRefreshToken());

                return ResponseEntity.ok()
                        .header(HttpHeaders.SET_COOKIE, newJwtCookie.toString())
                        .header(HttpHeaders.SET_COOKIE, newJwtRefreshCookie.toString())
                        .body(new MessageResponse("Token refreshed successfully!"));
            } catch (Exception e) {
                return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
            }
        }

        return ResponseEntity.badRequest().body(new MessageResponse("Refresh Token Cookie is empty!"));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser(HttpServletRequest request) {
        String refreshToken = jwtUtils.getJwtRefreshFromCookies(request);
        if (refreshToken != null && !refreshToken.isEmpty()) {
            try {
                RefreshToken token = refreshTokenService.findByToken(refreshToken).orElse(null);
                if (token != null) {
                    refreshTokenService.deleteByUserId(token.getUser().getId());
                }
            } catch (Exception e) {
                log.warning("Failed to delete refresh token on logout: " + e.getMessage());
            }
        }

        ResponseCookie jwtCookie = jwtUtils.getCleanJwtCookie();
        ResponseCookie jwtRefreshCookie = jwtUtils.getCleanRefreshJwtCookie();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                .header(HttpHeaders.SET_COOKIE, jwtRefreshCookie.toString())
                .body(new MessageResponse("Logged out successfully!"));
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        try {
            authService.registerUser(signUpRequest);
            return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        try {
            authService.processForgotPassword(request);
            return ResponseEntity.ok(new MessageResponse("Recovery link dispatched to your secure terminal."));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new MessageResponse("Error: " + e.getMessage()));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        try {
            authService.processResetPassword(request);
            return ResponseEntity.ok(new MessageResponse("Credentials updated. You may now re-authenticate."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new MessageResponse("An internal anomaly occurred: " + e.getMessage()));
        }
    }

    @GetMapping("/check-email")
    public ResponseEntity<?> checkEmail(@RequestParam String email) {
        boolean exists = authService.existsByEmail(email.trim().toLowerCase());
        return ResponseEntity.ok(java.util.Map.of("exists", exists));
    }
}
