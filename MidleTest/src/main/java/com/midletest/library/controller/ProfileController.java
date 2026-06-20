package com.midletest.library.controller;

import com.midletest.library.model.Reader;
import com.midletest.library.repository.BorrowRepository;
import com.midletest.library.repository.ReaderRepository;
import com.midletest.library.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ProfileController {
  private final BorrowRepository borrowRepository;
  private final UserRepository userRepository;
  private final ReaderRepository readerRepository;

  public ProfileController(
      BorrowRepository borrowRepository,
      UserRepository userRepository,
      ReaderRepository readerRepository) {
    this.borrowRepository = borrowRepository;
    this.userRepository = userRepository;
    this.readerRepository = readerRepository;
  }

  @GetMapping("/me")
  public String me(HttpSession session, Model model) {
    String currentUser = (String) session.getAttribute("currentUser");
    if (currentUser == null || currentUser.isBlank()) {
      return "redirect:/login";
    }

    String fullName = (String) session.getAttribute("currentUserFullName");
    Integer readerId = readerRepository.ensureReaderForUser(fullName, currentUser);
    Reader reader = readerRepository.findById(readerId).orElse(null);
    String readerEmail = resolveReaderEmail(currentUser);

    model.addAttribute("reader", reader);
    model.addAttribute("readerEmail", readerEmail);
    model.addAttribute("activeBorrows", borrowRepository.findActiveByReaderEmail(readerEmail, 50));
    model.addAttribute("recentBorrows", borrowRepository.findRecentByReaderEmail(readerEmail, 30));
    model.addAttribute("activeBorrowCount", borrowRepository.countActiveByReaderEmail(readerEmail));
    return "profile/me";
  }

  @PostMapping("/me/phone")
  public String updatePhone(
      @RequestParam("phone") String phone,
      HttpSession session,
      RedirectAttributes redirectAttributes) {
    String currentUser = (String) session.getAttribute("currentUser");
    if (currentUser == null || currentUser.isBlank()) {
      return "redirect:/login";
    }

    String normalizedPhone = normalizePhone(phone);
    if (normalizedPhone == null || !isValidPhone(normalizedPhone)) {
      redirectAttributes.addFlashAttribute("error", "Số điện thoại không hợp lệ.");
      return "redirect:/me";
    }

    String fullName = (String) session.getAttribute("currentUserFullName");
    Integer readerId = readerRepository.ensureReaderForUser(fullName, currentUser);
    readerRepository.updatePhone(readerId, normalizedPhone);
    redirectAttributes.addFlashAttribute("success", "Đã cập nhật số điện thoại.");
    return "redirect:/me";
  }

  private String resolveReaderEmail(String currentUser) {
    if (currentUser.contains("@")) {
      return currentUser.trim().toLowerCase();
    }
    return userRepository
        .findByUsernameOrEmail(currentUser.trim())
        .map(
            user -> {
              String email = user.getEmail();
              if (email != null && !email.isBlank()) {
                return email.trim().toLowerCase();
              }
              return currentUser.trim().toLowerCase() + "@library.local";
            })
        .orElse(currentUser.trim().toLowerCase() + "@library.local");
  }

  private String normalizePhone(String phone) {
    if (phone == null) {
      return null;
    }
    String normalized = phone.replaceAll("\\s+", "");
    return normalized.isBlank() ? null : normalized;
  }

  private boolean isValidPhone(String phone) {
    return phone.matches("^[+0-9]{9,15}$");
  }
}
