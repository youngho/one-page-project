package io.pinksoft.opp.config;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebHeadersConfig {

  @Bean
  public FilterRegistrationBean<Filter> googleSignInCoopFilter() {
    FilterRegistrationBean<Filter> registration = new FilterRegistrationBean<>();
    registration.setFilter(
        (ServletRequest request, ServletResponse response, FilterChain chain) -> {
          HttpServletResponse httpResponse = (HttpServletResponse) response;
          httpResponse.setHeader("Cross-Origin-Opener-Policy", "same-origin-allow-popups");
          chain.doFilter(request, response);
        });
    registration.addUrlPatterns("/*");
    registration.setOrder(0);
    return registration;
  }
}
