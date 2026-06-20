package com.midletest.library.controller;

import com.midletest.library.model.User;
import com.midletest.library.repository.ReaderRepository;
import com.midletest.library.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {
  private final UserRepository userRepository;
  private final ReaderRepository readerRepository;
  private final PasswordEncoder passwordEncoder;
  private final RestTemplate restTemplate = new RestTemplate();

  @Value("${app.google.client-id:}")
  private String googleClientId;

  public AuthController(
      UserRepository userRepository,
      ReaderRepository readerRepository,
      PasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.readerRepository = readerRepository;
    this.passwordEncoder = passwordEncoder;
  }

  @GetMapping("/login")
  public String loginForm(HttpSession session, Model model) {
    if (session.getAttribute("currentUser") != null) {
      return "redirect:/";
    }
    model.addAttribute("googleClientId", googleClientId == null ? "" : googleClientId);
    return "auth/login";
  }

  @PostMapping("/login")
  public String login(
      @RequestParam("email") String email,
      @RequestParam("password") String password,
      HttpSession session,
      RedirectAttributes redirectAttributes) {
    User user = userRepository.findByUsernameOrEmail(email.trim().toLowerCase()).orElse(null);
    if (user == null || !passwordEncoder.matches(password, user.getPasswordHash())) {
      redirectAttributes.addFlashAttribute("error", "Sai email hoặc mật khẩu.");
      return "redirect:/login";
    }

    createSession(session, user);
    if ("ADMIN".equalsIgnoreCase(user.getRole())) {
      return "redirect:/dashboard";
    }
    return "redirect:/";
  }

  @PostMapping("/auth/google")
  public String googleLogin(
      @RequestParam("credential") String credential,
      HttpSession session,
      RedirectAttributes redirectAttributes) {
    if (googleClientId == null || googleClientId.isBlank()) {
      redirectAttributes.addFlashAttribute("error", "Chưa cấu hình GOOGLE_CLIENT_ID.");
      return "redirect:/login";
    }
    if (credential == null || credential.isBlank()) {
      redirectAttributes.addFlashAttribute("error", "Không nhận được dữ liệu đăng nhập Google.");
      return "redirect:/login";
    }

    Map<?, ?> payload = verifyGoogleIdToken(credential);
    if (payload == null) {
      redirectAttributes.addFlashAttribute("error", "Đăng nhập Google thất bại. Vui lòng thử lại.");
      return "redirect:/login";
    }

    String email = String.valueOf(payload.get("email")).trim().toLowerCase();
    Object nameObj = payload.get("name");
    String name = nameObj == null ? "Google User" : String.valueOf(nameObj).trim();
    if (email.isBlank() || "null".equalsIgnoreCase(email)) {
      redirectAttributes.addFlashAttribute("error", "Google không trả về email hợp lệ.");
      return "redirect:/login";
    }

    User user =
        userRepository
            .findByEmail(email)
            .orElseGet(
                () -> {
                  String role = userRepository.countUsers() == 0 ? "ADMIN" : "USER";
                  String randomPasswordHash = passwordEncoder.encode(UUID.randomUUID().toString());
                  String fullName = name.isBlank() || "null".equalsIgnoreCase(name) ? email : name;
                  userRepository.create(email, randomPasswordHash, fullName, email, role);
                  readerRepository.ensureReaderForUser(fullName, email);
                  return userRepository.findByEmail(email).orElse(null);
                });

    if (user == null) {
      redirectAttributes.addFlashAttribute("error", "Không tạo được tài khoản Google.");
      return "redirect:/login";
    }

    createSession(session, user);
    if ("ADMIN".equalsIgnoreCase(user.getRole())) {
      return "redirect:/dashboard";
    }
    return "redirect:/";
  }

  @GetMapping("/register")
  public String registerForm(HttpSession session) {
    if (session.getAttribute("currentUser") != null) {
      return "redirect:/";
    }
    return "auth/register";
  }

  @PostMapping("/register")
  public String register(
      @RequestParam("fullName") String fullName,
      @RequestParam("email") String email,
      @RequestParam("phone") String phone,
      @RequestParam("password") String password,
      @RequestParam("confirmPassword") String confirmPassword,
      RedirectAttributes redirectAttributes) {
    String normalizedFullName = fullName.trim();
    String normalizedEmail = email.trim().toLowerCase();
    String normalizedPhone = normalizePhone(phone);
    String normalizedUsername = normalizedEmail;

    if (normalizedFullName.isEmpty()
        || normalizedEmail.isEmpty()
        || normalizedPhone == null
        || password.isBlank()) {
      redirectAttributes.addFlashAttribute("error", "Vui lòng nhập đầy đủ thông tin.");
      return "redirect:/register";
    }

    if (!normalizedEmail.contains("@")
        || normalizedEmail.startsWith("@")
        || normalizedEmail.endsWith("@")) {
      redirectAttributes.addFlashAttribute("error", "Email không hợp lệ.");
      return "redirect:/register";
    }

    if (!isValidPhone(normalizedPhone)) {
      redirectAttributes.addFlashAttribute("error", "Số điện thoại không hợp lệ.");
      return "redirect:/register";
    }

    if (!password.equals(confirmPassword)) {
      redirectAttributes.addFlashAttribute("error", "Mật khẩu xác nhận không khớp.");
      return "redirect:/register";
    }

    if (userRepository.existsByEmail(normalizedEmail)
        || userRepository.existsByUsername(normalizedUsername)) {
      redirectAttributes.addFlashAttribute("error", "Email đã được sử dụng.");
      return "redirect:/register";
    }

    String role = userRepository.countUsers() == 0 ? "ADMIN" : "USER";
    String passwordHash = passwordEncoder.encode(password);
    userRepository.create(normalizedUsername, passwordHash, normalizedFullName, normalizedEmail, role);
    readerRepository.ensureReaderForUser(normalizedFullName, normalizedEmail, normalizedPhone);

    if ("ADMIN".equals(role)) {
      redirectAttributes.addFlashAttribute(
          "success",
          "Đăng ký thành công tài khoản ADMIN đầu tiên. Vui lòng đăng nhập.");
    } else {
      redirectAttributes.addFlashAttribute("success", "Đăng ký thành công. Vui lòng đăng nhập.");
    }
    return "redirect:/login";
  }

  @GetMapping("/logout")
  public String logout(HttpSession session) {
    session.invalidate();
    return "redirect:/";
  }

  private void createSession(HttpSession session, User user) {
    session.setAttribute("currentUser", user.getUsername());
    session.setAttribute("currentUserFullName", user.getFullName());
    session.setAttribute("currentRole", user.getRole());
  }

  private Map<?, ?> verifyGoogleIdToken(String idToken) {
    try {
      String encodedToken = URLEncoder.encode(idToken, StandardCharsets.UTF_8);
      String url = "https://oauth2.googleapis.com/tokeninfo?id_token=" + encodedToken;
      @SuppressWarnings("unchecked")
      Map<String, Object> payload = restTemplate.getForObject(url, Map.class);
      if (payload == null) {
        return null;
      }
      String aud = String.valueOf(payload.get("aud"));
      String emailVerified = String.valueOf(payload.get("email_verified"));
      if (!googleClientId.equals(aud)) {
        return null;
      }
      if (!"true".equalsIgnoreCase(emailVerified)) {
        return null;
      }
      return payload;
    } catch (Exception ex) {
      return null;
    }
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
