package com.midletest.library.controller;

import com.midletest.library.model.Book;
import com.midletest.library.repository.BookRepository;
import com.midletest.library.repository.CategoryRepository;
import com.midletest.library.service.BookCoverService;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class BookController {
  private final BookRepository bookRepository;
  private final CategoryRepository categoryRepository;
  private final BookCoverService bookCoverService;

  public BookController(
      BookRepository bookRepository,
      CategoryRepository categoryRepository,
      BookCoverService bookCoverService) {
    this.bookRepository = bookRepository;
    this.categoryRepository = categoryRepository;
    this.bookCoverService = bookCoverService;
  }

  @GetMapping("/books")
  public String index(@RequestParam(value = "q", required = false) String q, Model model) {
    String keyword = q == null ? "" : q.trim();
    List<Book> books = keyword.isEmpty() ? bookRepository.findAll() : bookRepository.search(keyword);
    model.addAttribute("books", books);
    model.addAttribute("q", keyword);
    return "books/index";
  }

  @GetMapping("/books/create")
  public String create(Model model) {
    model.addAttribute("categories", categoryRepository.findAll());
    model.addAttribute("book", new Book());
    return "books/create";
  }

  @PostMapping("/books")
  public String store(@ModelAttribute Book book) {
    fillCoverIfMissing(book);
    bookRepository.create(book);
    return "redirect:/books";
  }

  @GetMapping("/books/edit/{id}")
  public String edit(@PathVariable Integer id, Model model) {
    Book book = bookRepository.findById(id).orElse(null);
    if (book == null) {
      return "redirect:/books";
    }
    model.addAttribute("book", book);
    model.addAttribute("categories", categoryRepository.findAll());
    return "books/edit";
  }

  @PostMapping("/books/update/{id}")
  public String update(@PathVariable Integer id, @ModelAttribute Book book) {
    fillCoverIfMissing(book);
    bookRepository.update(id, book);
    return "redirect:/books";
  }

  @PostMapping("/books/delete/{id}")
  public String delete(@PathVariable Integer id) {
    bookRepository.delete(id);
    return "redirect:/books";
  }

  private void fillCoverIfMissing(Book book) {
    if (book == null) {
      return;
    }
    String current = book.getCoverImage();
    if (current != null && !current.isBlank()) {
      return;
    }
    String autoCover = bookCoverService.findCoverImage(book.getTitle(), book.getAuthor());
    if (autoCover != null && !autoCover.isBlank()) {
      book.setCoverImage(autoCover);
    }
  }
}
