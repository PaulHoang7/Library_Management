package com.midletest.library.scheduler;

import com.midletest.library.repository.BorrowRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class BorrowStatusScheduler {
  private final BorrowRepository borrowRepository;

  public BorrowStatusScheduler(BorrowRepository borrowRepository) {
    this.borrowRepository = borrowRepository;
  }

  @Scheduled(fixedDelay = 60000)
  public void updateOverdueStatus() {
    borrowRepository.refreshOverdueFlags();
  }
}
