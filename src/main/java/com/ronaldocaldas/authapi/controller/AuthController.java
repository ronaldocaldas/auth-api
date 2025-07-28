package com.ronaldocaldas.authapi.controller;

import com.ronaldocaldas.authapi.model.RefreshToken;
import com.ronaldocaldas.authapi.repository.RefreshTokenRepository;
import com.ronaldocaldas.authapi.security.CustomOAuth2User;
import com.ronaldocaldas.authapi.security.oauth2.JwtTokenProvider;
import com.ronaldocaldas.authapi.service.RefreshTokenService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final JwtTokenProvider tokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final RefreshTokenRepository refreshTokenRepository;

    public AuthController(
            JwtTokenProvider tokenProvider,
            RefreshTokenService refreshTokenService,
            RefreshTokenRepository refreshTokenRepository
    ) {
        this.tokenProvider = tokenProvider;
        this.refreshTokenService = refreshTokenService;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @GetMapping("/login-success")
    public ResponseEntity<?> loginSuccess(Authentication authentication) {
        String userId;

        if (authentication.getPrincipal() instanceof CustomOAuth2User oauthUser) {
            userId = oauthUser.getName(); // ou outro campo como oauthUser.getEmail()
        } else {
            userId = authentication.getName();
        }

        String accessToken = tokenProvider.createToken(userId);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(userId);

        return ResponseEntity.ok(Map.of(
                "accessToken", accessToken,
                "refreshToken", refreshToken.getToken()
        ));
    }


    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody Map<String, String> request) {
        String requestToken = request.get("refreshToken");

        return refreshTokenRepository.findByToken(requestToken)
                .map(token -> {
                    if (refreshTokenService.isExpired(token)) {
                        refreshTokenService.deleteByUserId(token.getUserId());
                        return ResponseEntity.badRequest().body("Refresh token expired.");
                    }

                    String newAccessToken = tokenProvider.createToken(token.getUserId());
                    return ResponseEntity.ok(Map.of("accessToken", newAccessToken));
                })
                .orElse(ResponseEntity.badRequest().body("Invalid refresh token."));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");

        refreshTokenRepository.findByToken(refreshToken).ifPresent(token -> {
            refreshTokenService.deleteByUserId(token.getUserId());
        });

        return ResponseEntity.ok(Map.of("message", "Logged out successfully."));
    }

}
