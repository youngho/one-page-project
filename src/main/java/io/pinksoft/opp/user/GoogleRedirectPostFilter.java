package io.pinksoft.opp.user;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Google redirect 로그인이 POST /opp/ 로 credential을 보낼 때 처리한다.
 * {@code @PostMapping("/")} 는 GET /opp/ 정적 페이지까지 405로 막으므로 필터로만 처리한다.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 5)
@RequiredArgsConstructor
public class GoogleRedirectPostFilter implements Filter {

    private final GoogleAuthRedirectHandler redirectHandler;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (!(request instanceof HttpServletRequest req) || !(response instanceof HttpServletResponse res)) {
            chain.doFilter(request, response);
            return;
        }

        if (!"POST".equalsIgnoreCase(req.getMethod()) || req.getParameter("credential") == null) {
            chain.doFilter(request, response);
            return;
        }

        String servletPath = req.getServletPath();
        if (!"/".equals(servletPath) && !servletPath.isEmpty()) {
            chain.doFilter(request, response);
            return;
        }

        String target = redirectHandler.redirectUrlAfterLogin(req.getParameter("credential"));
        res.sendRedirect(req.getContextPath() + target);
    }
}
