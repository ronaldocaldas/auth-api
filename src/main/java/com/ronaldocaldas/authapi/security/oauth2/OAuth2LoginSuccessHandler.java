package com.ronaldocaldas.authapi.security.oauth2;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ronaldocaldas.authapi.model.RefreshToken;
import com.ronaldocaldas.authapi.repository.UserRepository;
import com.ronaldocaldas.authapi.security.CustomOAuth2User;
import com.ronaldocaldas.authapi.service.RefreshTokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtTokenProvider tokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final UserRepository userRepository;

    public OAuth2LoginSuccessHandler(JwtTokenProvider tokenProvider,
                                     RefreshTokenService refreshTokenService,
                                     UserRepository userRepository) {
        this.tokenProvider = tokenProvider;
        this.refreshTokenService = refreshTokenService;
        this.userRepository = userRepository;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        Object principal = authentication.getPrincipal();
        String userId = null;

        if (principal instanceof CustomOAuth2User customUser) {
            userId = customUser.getUser().getId();
        } else if (principal instanceof DefaultOidcUser oidcUser) {
            userId = (String) oidcUser.getAttributes().get("email");
        } else if (principal instanceof OAuth2User oauth2User) {
            userId = (String) oauth2User.getAttributes().get("email");
        }

        if (userId == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unable to extract user information");
            return;
        }

        String accessToken = tokenProvider.createToken(userId);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(userId);

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        String json = new ObjectMapper().writeValueAsString(Map.of(
                "accessToken", accessToken,
                "refreshToken", refreshToken.getToken()
        ));
        response.getWriter().write(json);
    }
}
