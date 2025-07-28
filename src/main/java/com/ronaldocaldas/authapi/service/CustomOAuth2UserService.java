package com.ronaldocaldas.authapi.service;

import com.ronaldocaldas.authapi.model.User;
import com.ronaldocaldas.authapi.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class CustomOAuth2UserService extends OidcUserService {

    private final UserRepository userRepository;
    private static final Logger log = LoggerFactory.getLogger(CustomOAuth2UserService.class);

    public CustomOAuth2UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        OidcUser oidcUser = super.loadUser(userRequest);

        String email = oidcUser.getEmail();
        String name = oidcUser.getFullName(); // ou: oidcUser.getAttribute("name")

        log.info("OIDC User Info: name={}, email={}", name, email);

        if (email == null) {
            throw new OAuth2AuthenticationException("Email not found from provider");
        }

        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            user = new User(UUID.randomUUID().toString(), name, email);
            userRepository.save(user);
            log.info("Saved new user: {}", user);
        } else {
            log.info("User already exists: {}", user);
        }

        return oidcUser;
    }
}