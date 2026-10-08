package com.florvvia.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.*;

import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

@Configuration
public class WebConfig implements WebMvcConfigurer {
  @Value("${app.upload-dir:./uploads}")
  private String uploadDir;

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    // Photos: safe to cache for a week (uploaded files get unique names, never change).
    CacheControl week = CacheControl.maxAge(7, TimeUnit.DAYS).cachePublic();
    String loc = Paths.get(uploadDir).toAbsolutePath().toUri().toString();
    registry.addResourceHandler("/uploads/**").addResourceLocations(loc).setCacheControl(week);
    registry.addResourceHandler("/images/**").addResourceLocations("classpath:/static/images/").setCacheControl(week);
    // Pages + scripts: always revalidate so the browser never runs a stale UI.
    registry.addResourceHandler("/**").addResourceLocations("classpath:/static/")
      .setCacheControl(CacheControl.noCache());
  }

  @Override
  public void addCorsMappings(CorsRegistry registry) {
    // Lets the hosted frontend (e.g. Netlify) call this API cross-origin.
    registry.addMapping("/api/**")
      .allowedOriginPatterns("*")
      .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
      .allowedHeaders("*")
      .maxAge(3600);
  }
}
