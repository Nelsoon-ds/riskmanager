package com.nelson.riskmanager.config;

import com.nelson.riskmanager.service.UserLoginService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@EnableWebSecurity
@Configuration
public class SecurityConfig {

    private final UserLoginService userLoginService;

    public SecurityConfig(UserLoginService userLoginService) {
        this.userLoginService = userLoginService;
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
                .oauth2Login(oauth -> oauth
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(userLoginService)
                        )
                        .defaultSuccessUrl("/analyze", true))
                .logout((logout) -> logout.logoutUrl("/"))
                .formLogin(Customizer.withDefaults())
                .build();
    }

}
