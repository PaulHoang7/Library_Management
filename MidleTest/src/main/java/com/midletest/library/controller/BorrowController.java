package com.midletest.library.controller;

import com.midletest.library.model.Book;
import com.midletest.library.model.Borrow;
import com.midletest.library.repository.BookRepository;
import com.midletest.library.repository.BorrowRepository;
import com.midletest.library.repository.ReaderRepository;
import com.midletest.library.repository.UserRepository;
import com.midletest.library.service.BorrowNotificationService;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class BorrowController {
  private final BorrowRepository borrowRepository;
  private final BookRepository bookRepository;
  private final ReaderRepository readerRepository;
  private final UserRepository userRepository;
  private final BorrowNotificationService borrowNotificationService;

  public BorrowController(
      BorrowRepository borrowRepository,
      BookRepository bookRepository,
      ReaderRepository readerRepository,
      UserRepository userRepository,
      BorrowNotificationService borrowNotificationService) {
    this.borrowRepository = borrowRepository;
    this.bookRepository = bookRepository;
    this.readerRepository = readerRepository;
    this.userRepository = userRepository;
    this.borrowNotificationService = borrowNotificationService;
  }

  @GetMapping("/borrows")
  public String index(Model model) {
    borrowRepository.refreshOverdueFlags();
    model.addAttribute("borrows", borrowRepository.findAll());
    return "borrows/index";
  }

  @GetMapping("/borrows/create")
  public String create(@RequestParam(value = "bookId", required = false) Integer bookId, Model model) {
    model.addAttribute("books", bookRepository.findAll());
    model.addAttribute("readers", readerRepository.findAll());
    model.addAttribute("hasAvailableBooks", bookRepository.countAvailable() > 0);
    Borrow borrow = new Borrow();
    borrow.setBorrowDate(LocalDate.now());
    borrow.setBorrowDays(14);
    if (bookId != null) {
      borrow.setBookId(bookId);
    }
    model.addAttribute("borrow", borrow);
    model.addAttribute("requestMode", false);
    return "borrows/create";
  }

  @PostMapping("/borrows")
  public String store(@ModelAttribute Borrow borrow, RedirectAttributes redirectAttributes) {
    try {
      borrowRepository.create(borrow);
      redirectAttributes.addFlashAttribute("success", "Tạo phiếu mượn thành công.");
    } catch (IllegalStateException ex) {
      redirectAttributes.addFlashAttribute("error", ex.getMessage());
      return "redirect:/borrows/create";
    }
    return "redirect:/borrows";
  }

  @GetMapping("/borrow/request")
  public String borrowRequest(
      @RequestParam(value = "bookId", required = false) Integer bookId,
      HttpSession session,
      RedirectAttributes redirectAttributes,
      Model model) {
    Integer readerId = resolveReaderId(session);
    if (readerId == null) {
      return "redirect:/login";
    }
    if (!hasValidPhone(readerId)) {
      redirectAttributes.addFlashAttribute(
          "error",
          "Bạn cần cập nhật số điện thoại trong Trang cá nhân trước khi mượn sách.");
      return "redirect:/me";
    }

    Borrow borrow = new Borrow();
    borrow.setReaderId(readerId);
    borrow.setBorrowDate(LocalDate.now());
    borrow.setBorrowDays(14);
    if (bookId != null) {
      borrow.setBookId(bookId);
    }

    model.addAttribute("books", bookRepository.findAll());
    model.addAttribute("hasAvailableBooks", bookRepository.countAvailable() > 0);
    model.addAttribute("borrow", borrow);
    model.addAttribute("requestMode", true);
    return "borrows/create";
  }

  @PostMapping("/borrow/request")
  public String storeBorrowRequest(
      @ModelAttribute Borrow borrow,
      HttpSession session,
      RedirectAttributes redirectAttributes) {
    Integer readerId = resolveReaderId(session);
    if (readerId == null) {
      return "redirect:/login";
    }
    if (!hasValidPhone(readerId)) {
      redirectAttributes.addFlashAttribute(
          "error",
          "Bạn cần cập nhật số điện thoại trong Trang cá nhân trước khi mượn sách.");
      return "redirect:/me";
    }
    borrow.setReaderId(readerId);
    String userEmail = resolveUserEmail(session);
    String userName = resolveUserName(session);
    String bookTitle =
        bookRepository
            .findById(borrow.getBookId())
            .map(Book::getTitle)
            .orElse("Mã sách " + borrow.getBookId());
    int requestedDays = borrow.getBorrowDays() == null ? 14 : borrow.getBorrowDays();
    LocalDate borrowDate = borrow.getBorrowDate() == null ? LocalDate.now() : borrow.getBorrowDate();
    LocalDate expectedDueDate = borrowDate.plusDays(requestedDays);
    try {
      borrowRepository.create(borrow);
      redirectAttributes.addFlashAttribute("success", "Mượn sách thành công.");
      notifyBorrowResult(userEmail, userName, bookTitle, requestedDays, expectedDueDate, true, null);
    } catch (IllegalStateException ex) {
      redirectAttributes.addFlashAttribute("error", ex.getMessage());
      notifyBorrowResult(userEmail, userName, bookTitle, requestedDays, expectedDueDate, false, ex.getMessage());
    }
    return "redirect:/catalog";
  }

  @PostMapping("/borrows/return/{id}")
  public String returnBook(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
    boolean returned = borrowRepository.returnBook(id);
    if (returned) {
      redirectAttributes.addFlashAttribute("success", "Trả sách thành công. Tồn kho đã được cập nhật.");
    } else {
      redirectAttributes.addFlashAttribute("error", "Phiếu mượn không hợp lệ hoặc đã trả trước đó.");
    }
    return "redirect:/borrows";
  }

  private Integer resolveReaderId(HttpSession session) {
    String username = (String) session.getAttribute("currentUser");
    if (username == null || username.isBlank()) {
      return null;
    }
    String fullName = (String) session.getAttribute("currentUserFullName");
    return readerRepository.ensureReaderForUser(fullName, username);
  }

  private boolean hasValidPhone(Integer readerId) {
    if (readerId == null) {
      return false;
    }
    return readerRepository
        .findById(readerId)
        .map(reader -> reader.getPhone() != null && !reader.getPhone().trim().isBlank())
        .orElse(false);
  }

  private String resolveUserEmail(HttpSession session) {
    String currentUser = (String) session.getAttribute("currentUser");
    if (currentUser == null || currentUser.isBlank()) {
      return null;
    }
    if (currentUser.contains("@")) {
      return currentUser.trim().toLowerCase();
    }
    return userRepository
        .findByUsernameOrEmail(currentUser.trim())
        .map(user -> user.getEmail() == null ? null : user.getEmail().trim().toLowerCase())
        .orElse(null);
  }

  private String resolveUserName(HttpSession session) {
    String fullName = (String) session.getAttribute("currentUserFullName");
    if (fullName != null && !fullName.isBlank()) {
      return fullName.trim();
    }
    String currentUser = (String) session.getAttribute("currentUser");
    return currentUser == null ? "bạn" : currentUser;
  }

  private void notifyBorrowResult(
      String userEmail,
      String userName,
      String bookTitle,
      Integer borrowDays,
      LocalDate dueDate,
      boolean success,
      String reason) {
    if (userEmail == null || userEmail.isBlank()) {
      return;
    }
    try {
      borrowNotificationService.sendBorrowResult(
          userEmail,
          userName,
          bookTitle,
          borrowDays,
          dueDate,
          success,
          reason);
    } catch (Exception ignored) {
      // Keep borrow flow stable even if email notification fails.
    }
  }
}
