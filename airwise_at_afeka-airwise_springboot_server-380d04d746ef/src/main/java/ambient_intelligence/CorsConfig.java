package ambient_intelligence;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

  @Override
  public void addCorsMappings(CorsRegistry registry) {
    registry.addMapping("/ambient-intelligence/**")
      .allowedOrigins("*") // TODO: change to the client host link:port
      .allowedMethods("OPTIONS", "GET", "POST", "PUT", "DELETE")
      .allowedHeaders("Authorization", "Content-Type")
      .maxAge(3600);
  }
}
