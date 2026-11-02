package com.project.sneakermarket.common.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        HttpSessionCsrfTokenRepository csrfTokenRepository =
                new HttpSessionCsrfTokenRepository();
        csrfTokenRepository.setHeaderName("X-CSRF-TOKEN");

        return http
                // Session Cookie 架構必須使用 CSRF Token。只有第三方金流
                // Server-to-Server 回呼無法取得 Token，因此以簽章驗證取代。
                .csrf(csrf -> csrf
                        .csrfTokenRepository(csrfTokenRepository)
                        .ignoringRequestMatchers("/api/payment/notify")
                )

                // 使用既有 CorsConfig
                .cors(Customizer.withDefaults())

                // 需要時才建立 Session，允許登入狀態保存在 HttpSession。
                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.IF_REQUIRED
                        )
                )

                // 專案使用自訂 HttpSession memberId，而非 Spring
                // Authentication；會員與管理員授權由各 API 的共用
                // SessionAuthorizationService 執行。
                .authorizeHttpRequests(auth ->
                        auth.anyRequest().permitAll()
                )

                .build();
    }
}
