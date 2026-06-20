package com.midletest.library.config;

import com.midletest.library.repository.BorrowRepository;
import com.midletest.library.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice(annotations = Controller.class)
public class GlobalModelAttributes {
  private final BorrowRepository borrowRepository;
  private final UserRepository userRepository;

  public GlobalModelAttributes(BorrowRepository borrowRepository, UserRepository userRepository) {
    this.borrowRepository = borrowRepository;
    this.userRepository = userRepository;
  }

  @ModelAttribute
  public void addCommonAttributes(HttpServletRequest request, Model model) {
    model.addAttribute("currentPath", request.getRequestURI());
    HttpSession session = request.getSession(false);
    String currentUser = session == null ? null : (String) session.getAttribute("currentUser");
    String currentUserFullName =
        session == null ? null : (String) session.getAttribute("currentUserFullName");
    String currentRole = session == null ? null : (String) session.getAttribute("currentRole");
    model.addAttribute("currentUser", currentUser);
    model.addAttribute("currentUserFullName", currentUserFullName);
    model.addAttribute("currentRole", currentRole);
    model.addAttribute("isAdmin", "ADMIN".equalsIgnoreCase(currentRole));
    model.addAttribute("currentBorrowedCount", borrowRepository.countActiveByReaderEmail(resolveReaderEmail(currentUser)));
  }

  private String resolveReaderEmail(String currentUser) {
    if (currentUser == null || currentUser.isBlank()) {
      return null;
    }
    if (currentUser.contains("@")) {
      return currentUser.trim().toLowerCase();
    }
    return userRepository.findByUsernameOrEmail(currentUser.trim())
        .map(user -> {
          String email = user.getEmail();
          if (email != null && !email.isBlank()) {
            return email.trim().toLowerCase();
          }
          return currentUser.trim().toLowerCase() + "@library.local";
        })
        .orElse(currentUser.trim().toLowerCase() + "@library.local");
  }
}
