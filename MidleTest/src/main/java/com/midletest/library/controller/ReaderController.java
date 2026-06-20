package com.midletest.library.controller;

import com.midletest.library.model.Reader;
import com.midletest.library.repository.ReaderRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class ReaderController {
  private final ReaderRepository readerRepository;

  public ReaderController(ReaderRepository readerRepository) {
    this.readerRepository = readerRepository;
  }

  @GetMapping("/readers")
  public String index(Model model) {
    model.addAttribute("readers", readerRepository.findAll());
    return "readers/index";
  }

  @GetMapping("/readers/create")
  public String create(Model model) {
    model.addAttribute("reader", new Reader());
    return "readers/create";
  }

  @PostMapping("/readers")
  public String store(@ModelAttribute Reader reader) {
    readerRepository.create(reader);
    return "redirect:/readers";
  }

  @GetMapping("/readers/edit/{id}")
  public String edit(@PathVariable Integer id, Model model) {
    Reader reader = readerRepository.findById(id).orElse(null);
    if (reader == null) {
      return "redirect:/readers";
    }
    model.addAttribute("reader", reader);
    return "readers/edit";
  }

  @PostMapping("/readers/update/{id}")
  public String update(@PathVariable Integer id, @ModelAttribute Reader reader) {
    readerRepository.update(id, reader);
    return "redirect:/readers";
  }

  @PostMapping("/readers/delete/{id}")
  public String delete(@PathVariable Integer id) {
    readerRepository.delete(id);
    return "redirect:/readers";
  }
}
