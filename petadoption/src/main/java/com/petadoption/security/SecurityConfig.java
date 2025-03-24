package com.petadoption.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private static final String ROLE_ADMIN = "ADMIN";
    private static final String API_PETS_PATTERN = "/api/pets/**";

    private final JwtAuthFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter, UserDetailsService userDetailsService) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
            .csrf(csrf -> csrf.disable()) // CSRF disabled since app is stateless REST API
            .authorizeHttpRequests(auth -> auth

                // Static resources and public pages
                .requestMatchers("/css/**", "/js/**", "/images/**", "/static/**", "/img/**").permitAll()
                .requestMatchers("/", "/index.html", "/login.html", "/register.html").permitAll()

                // Public auth and upload endpoints
                .requestMatchers("/auth/**").permitAll()
                .requestMatchers("/uploads/**").permitAll()
                .requestMatchers("/h2-console/**").permitAll()

                // Admin-only access
                .requestMatchers("/api/dashboard/admin/**").hasRole(ROLE_ADMIN)
                .requestMatchers(HttpMethod.PUT, API_PETS_PATTERN).hasAuthority(ROLE_ADMIN)
                .requestMatchers(HttpMethod.DELETE, API_PETS_PATTERN).hasAuthority(ROLE_ADMIN)
                .requestMatchers("/api/admin/adoptions/**").hasAuthority(ROLE_ADMIN)

                // User-specific access
                .requestMatchers("/api/adoptions/my-pets", "/api/adoptions/my-requests", "/api/adoptions/pending-requests").hasAuthority("USER")
                .requestMatchers("/api/adoptions/**").authenticated()
                .requestMatchers(API_PETS_PATTERN).authenticated()

                // Catch-all
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
            .build();
    }
}
