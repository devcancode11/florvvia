package com.florvvia.config;

import com.florvvia.security.JwtFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {
  @Autowired private JwtFilter jwtFilter;

  @Bean
  public PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.csrf(csrf -> csrf.disable())
      .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
      .authorizeHttpRequests(a -> a
        .requestMatchers("/", "/index.html", "/css/**", "/js/**", "/images/**", "/uploads/**",
          "/h2-console/**", "/api/auth/**", "/api/products", "/api/products/**",
          "/api/reviews/product/**", "/api/config", "/error").permitAll()
        .requestMatchers("/api/admin/**").hasRole("ADMIN")
        .anyRequest().permitAll()
      )
      .headers(h -> h.frameOptions(f -> f.disable()));
    http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
    return http.build();
  }
}
