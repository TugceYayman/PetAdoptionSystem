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

    // BCrypt Password Encoder Bean
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Authentication Manager Bean
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    // Authentication Provider (binds UserDetailsService and PasswordEncoder together)
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    // Security Filter Chain (specifies which routes are public/protected)
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())  // Disable CSRF for API (adjust if needed)

                .authorizeHttpRequests(auth -> auth
                        // Allow all static assets (HTML, JS, CSS, images)
                        .requestMatchers("/css/**", "/js/**", "/images/**").permitAll()
                        .requestMatchers(
                                "/", // This is handled by WebController (redirect to login.html)
                                "/index.html", 
                                "/login.html", 
                                "/register.html", 
                                "/my-adoptions.html", 
                                "/admin-dashboard.html"
                        ).permitAll()
                        // Allow authentication and H2 console (optional for testing)
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers("/h2-console/**").permitAll()

                        // Protect your API endpoints
                        .requestMatchers("/api/pets/**").hasAuthority("ADMIN")
                        .requestMatchers("/api/adoptions/**").hasAuthority("USER")

                        // Any other request requires authentication
                        .anyRequest().authenticated()
                )
                // Add JWT filter before standard Username/Password filter
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
