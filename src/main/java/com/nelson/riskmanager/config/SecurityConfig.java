package com.nelson.riskmanager.config;

import com.nelson.riskmanager.oAuth.OAuthSuccessHandler;
import com.nelson.riskmanager.service.UserLoginService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@EnableWebSecurity
@Configuration
public class SecurityConfig {

    private final UserLoginService userLoginService;
    private final OAuthSuccessHandler oAuthSuccessHandler;

    public SecurityConfig(UserLoginService userLoginService, OAuthSuccessHandler oAuthSuccessHandler) {
        this.userLoginService = userLoginService;
        this.oAuthSuccessHandler = oAuthSuccessHandler;
    }


    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) {
        return http
                .authorizeHttpRequests(auth -> {
                    auth
                            .requestMatchers("/css/**", "/js/**", "/images/**").permitAll()
                            .requestMatchers("/", "/login").permitAll();
                            auth.anyRequest().authenticated();
                })
                .logout((logout) -> logout.logoutUrl("/"))
                .oauth2Login(oauth -> oauth
                        .successHandler(oAuthSuccessHandler)
                        .loginPage("/login")

                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(userLoginService)
                        )
                )
                .build();
    }

}
