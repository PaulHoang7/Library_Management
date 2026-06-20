package com.midletest.library.service;

import java.time.LocalDate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class BorrowNotificationService {
  private final JavaMailSender mailSender;

  @Value("${app.mail.from:}")
  private String fromEmail;

  public BorrowNotificationService(JavaMailSender mailSender) {
    this.mailSender = mailSender;
  }

  public void sendBorrowResult(
      String toEmail,
      String fullName,
      String bookTitle,
      Integer borrowDays,
      LocalDate dueDate,
      boolean success,
      String reason) {
    String safeName = (fullName == null || fullName.isBlank()) ? "bạn" : fullName;
    String safeBook = (bookTitle == null || bookTitle.isBlank()) ? "không xác định" : bookTitle;
    int safeDays = borrowDays == null ? 14 : borrowDays;

    String subject = success ? "Thông báo mượn sách thành công" : "Thông báo mượn sách không thành công";
    String text;
    if (success) {
      text =
          "Xin chào " + safeName + ",\n\n"
              + "Yêu cầu mượn sách của bạn đã được ghi nhận thành công.\n"
              + "Sách: " + safeBook + "\n\n"
              + "Số ngày mượn: " + safeDays + " ngày\n"
              + "Hạn trả dự kiến: " + (dueDate == null ? "chưa xác định" : dueDate) + "\n\n"
              + "Cảm ơn bạn đã sử dụng hệ thống thư viện.";
    } else {
      String safeReason = (reason == null || reason.isBlank()) ? "Không rõ lý do." : reason;
      text =
          "Xin chào " + safeName + ",\n\n"
              + "Yêu cầu mượn sách của bạn không thể thực hiện.\n"
              + "Sách: " + safeBook + "\n"
              + "Số ngày mượn đăng ký: " + safeDays + " ngày\n"
              + "Lý do: " + safeReason + "\n\n"
              + "Vui lòng thử lại sau.";
    }

    SimpleMailMessage message = new SimpleMailMessage();
    if (fromEmail != null && !fromEmail.isBlank()) {
      message.setFrom(fromEmail);
    }
    message.setTo(toEmail);
    message.setSubject(subject);
    message.setText(text);
    mailSender.send(message);
  }
}
