package com.florvvia.security;

import com.florvvia.model.User;
import com.florvvia.repository.UserRepository;
import io.jsonwebtoken.Claims;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.List;

@Component
public class JwtFilter extends OncePerRequestFilter {
  @Autowired private JwtUtil jwtUtil;
  @Autowired private UserRepository users;

  @Override
  protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
      throws ServletException, IOException {
    String h = req.getHeader("Authorization");
    if (h != null && h.startsWith("Bearer ")) {
      try {
        Claims c = jwtUtil.parse(h.substring(7));
        Long id = Long.valueOf(c.getSubject());
        String role = c.get("role", String.class);
        User u = users.findById(id).orElse(null);
        if (u != null) {
          var auth = new UsernamePasswordAuthenticationToken(u, null,
            List.of(new SimpleGrantedAuthority("ROLE_" + (role == null ? u.getRole() : role))));
          SecurityContextHolder.getContext().setAuthentication(auth);
        }
      } catch (Exception ignored) {}
    }
    chain.doFilter(req, res);
  }
}
