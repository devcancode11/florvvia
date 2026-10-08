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
    String loc = Paths.get(uploadDir).toAbsolutePath().toUri().toString();
    registry.addResourceHandler("/uploads/**").addResourceLocations(loc)
      .setCacheControl(CacheControl.maxAge(7, TimeUnit.DAYS).cachePublic());
  }
}
