package io.pinksoft.opp.config;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class WebHeadersConfig {

  /**
   * Google Sign-In 팝업/FedCM이 postMessage로 부모 창과 통신할 수 있도록 COOP를 완화한다.
   * @see <a href="https://developers.google.com/identity/gsi/web/guides/get-google-api-clientid">GIS setup</a>
   */
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
