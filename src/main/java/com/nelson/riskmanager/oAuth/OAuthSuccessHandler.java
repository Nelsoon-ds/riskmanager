package com.nelson.riskmanager.oAuth;

import com.nelson.riskmanager.model.User;
import com.nelson.riskmanager.service.UserLoginService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class OAuthSuccessHandler implements AuthenticationSuccessHandler {

    private final UserLoginService userLoginService;

    public OAuthSuccessHandler(UserLoginService userLoginService) {
        this.userLoginService = userLoginService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        @NotNull Authentication authentication) throws IOException {
        OAuth2AuthenticationToken token = (OAuth2AuthenticationToken) authentication;
        User user = userLoginService.saveOrGetUser(token);
        // Store in session
        request.getSession().setAttribute("currentUser", user);

        response.sendRedirect("/analyze");
    }
}