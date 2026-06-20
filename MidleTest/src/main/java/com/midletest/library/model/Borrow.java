package com.midletest.library.model;

import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;

public class Borrow {
  private Integer id;
  private Integer readerId;
  private Integer bookId;
  private String readerName;
  private String bookTitle;

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
  private LocalDate borrowDate;

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
  private LocalDate returnDate;

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
  private LocalDate dueDate;

  private Integer borrowDays;

  private String status;
  private boolean overdue;

  public Integer getId() { return id; }
  public void setId(Integer id) { this.id = id; }
  public Integer getReaderId() { return readerId; }
  public void setReaderId(Integer readerId) { this.readerId = readerId; }
  public Integer getBookId() { return bookId; }
  public void setBookId(Integer bookId) { this.bookId = bookId; }
  public String getReaderName() { return readerName; }
  public void setReaderName(String readerName) { this.readerName = readerName; }
  public String getBookTitle() { return bookTitle; }
  public void setBookTitle(String bookTitle) { this.bookTitle = bookTitle; }
  public LocalDate getBorrowDate() { return borrowDate; }
  public void setBorrowDate(LocalDate borrowDate) { this.borrowDate = borrowDate; }
  public LocalDate getReturnDate() { return returnDate; }
  public void setReturnDate(LocalDate returnDate) { this.returnDate = returnDate; }
  public LocalDate getDueDate() { return dueDate; }
  public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
  public Integer getBorrowDays() { return borrowDays; }
  public void setBorrowDays(Integer borrowDays) { this.borrowDays = borrowDays; }
  public String getStatus() { return status; }
  public void setStatus(String status) { this.status = status; }
  public boolean isOverdue() { return overdue; }
  public void setOverdue(boolean overdue) { this.overdue = overdue; }
}
