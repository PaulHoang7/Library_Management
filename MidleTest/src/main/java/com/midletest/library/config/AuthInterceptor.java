package com.midletest.library.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
      throws Exception {
    String path = request.getRequestURI();

    if (isPublicPath(path)) {
      return true;
    }

    HttpSession session = request.getSession(false);
    String currentUser = session == null ? null : (String) session.getAttribute("currentUser");
    String currentRole = session == null ? null : (String) session.getAttribute("currentRole");

    if (currentUser == null) {
      response.sendRedirect("/login");
      return false;
    }

    if (isAdminOnly(path) && !"ADMIN".equalsIgnoreCase(currentRole)) {
      response.sendRedirect("/");
      return false;
    }

    return true;
  }

  private boolean isPublicPath(String path) {
    return "/".equals(path)
        || "/catalog".equals(path)
        || "/login".equals(path)
        || "/auth/google".equals(path)
        || "/register".equals(path)
        || path.startsWith("/css/")
        || path.startsWith("/js/")
        || path.startsWith("/images/")
        || "/favicon.ico".equals(path)
        || "/error".equals(path);
  }

  private boolean isAdminOnly(String path) {
    return path.startsWith("/dashboard")
        || path.startsWith("/readers")
        || path.startsWith("/books")
        || path.startsWith("/borrows");
  }
}
