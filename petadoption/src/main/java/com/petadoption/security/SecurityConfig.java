package com.petadoption.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.core.userdetails.UserDetailsService;

@Configuration
public class SecurityConfig {

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
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                // Public pages (login/register pages and static assets)
                .requestMatchers("/css/**", "/js/**", "/images/**").permitAll()
                .requestMatchers(
                        "/",
                        "/index.html",
                        "/login.html",
                        "/register.html"
                ).permitAll()

                // Public auth endpoints
                .requestMatchers("/auth/**").permitAll()

                // H2 Console (for local development)
                .requestMatchers("/h2-console/**").permitAll()

                // Role-specific API access
                .requestMatchers("/api/dashboard/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/dashboard/csr/**").hasRole("CSR")
                .requestMatchers("/api/dashboard/support/**").hasRole("SUPPORT")
                .requestMatchers("/api/dashboard/network/**").hasRole("NETWORK")

                // Regular protected API access
                .requestMatchers("/api/pets/**").hasAuthority("ADMIN")
                .requestMatchers("/api/adoptions/**").hasAuthority("USER")

                // Catch-all: any other requests must be authenticated
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
            .build();
    }
}
