package com.midletest.library.controller;

import com.midletest.library.model.Book;
import com.midletest.library.model.Borrow;
import com.midletest.library.repository.BookRepository;
import com.midletest.library.repository.BorrowRepository;
import com.midletest.library.repository.ReaderRepository;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
  private final BookRepository bookRepository;
  private final ReaderRepository readerRepository;
  private final BorrowRepository borrowRepository;

  public HomeController(
      BookRepository bookRepository,
      ReaderRepository readerRepository,
      BorrowRepository borrowRepository) {
    this.bookRepository = bookRepository;
    this.readerRepository = readerRepository;
    this.borrowRepository = borrowRepository;
  }

  @GetMapping("/")
  public String landing(Model model) {
    List<Book> featuredBooks = bookRepository.findFeatured(8);
    model.addAttribute("featuredBooks", featuredBooks);
    return "home";
  }

  @GetMapping("/catalog")
  public String catalog(Model model) {
    model.addAttribute("books", bookRepository.findAll());
    return "catalog";
  }

  @GetMapping("/dashboard")
  public String dashboard(Model model) {
    borrowRepository.refreshOverdueFlags();
    List<Borrow> allBorrows = borrowRepository.findAll();
    List<Borrow> recentBorrows = allBorrows.stream().limit(8).toList();
    List<Borrow> overdueBorrows =
        allBorrows.stream()
            .filter(b -> "borrowed".equalsIgnoreCase(b.getStatus()) && b.isOverdue())
            .limit(8)
            .toList();

    List<Book> lowStockBooks =
        bookRepository.findAll().stream().filter(book -> book.getQuantity() != null && book.getQuantity() <= 2).limit(8).toList();

    model.addAttribute("totalBooks", bookRepository.count());
    model.addAttribute("totalReaders", readerRepository.count());
    model.addAttribute("totalBorrowed", borrowRepository.countBorrowed());
    model.addAttribute("totalOverdue", borrowRepository.countOverdue());
    model.addAttribute("recentBorrows", recentBorrows);
    model.addAttribute("overdueBorrows", overdueBorrows);
    model.addAttribute("lowStockBooks", lowStockBooks);
    return "dashboard";
  }
}
