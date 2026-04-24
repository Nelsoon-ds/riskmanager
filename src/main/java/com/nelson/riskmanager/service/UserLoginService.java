package com.nelson.riskmanager.service;

import com.nelson.riskmanager.model.User;
import com.nelson.riskmanager.repository.RiskManagerRepository;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UserLoginService extends DefaultOAuth2UserService {

    private final RiskManagerRepository userRepository;

    public UserLoginService(RiskManagerRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest request) throws OAuth2AuthenticationException {
        OAuth2User oauthUser = super.loadUser(request);

        String provider = request.getClientRegistration().getRegistrationId(); // "github" or "google"
        String oauthId = oauthUser.getName();
        String name = oauthUser.getAttribute("name");
        String email = oauthUser.getAttribute("email");

        // Find or create
        userRepository.findByOauthIdAndProvider(oauthId, provider)
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setOauthId(oauthId);
                    newUser.setProvider(provider);
                    newUser.setName(name);
                    newUser.setEmail(email);
                    newUser.setCreatedAt(LocalDateTime.now());
                    return userRepository.saveUser(newUser);
                });




        return oauthUser; // Spring Security still needs this
    }
}
