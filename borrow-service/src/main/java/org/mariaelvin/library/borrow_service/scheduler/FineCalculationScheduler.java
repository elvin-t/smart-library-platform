package org.mariaelvin.library.borrow_service.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mariaelvin.library.borrow_service.entity.BorrowRecord;
import org.mariaelvin.library.borrow_service.entity.BorrowStatus;
import org.mariaelvin.library.borrow_service.repository.BorrowRecordRepository;
import org.mariaelvin.library.borrow_service.service.FineCalculationService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class FineCalculationScheduler {

    private final BorrowRecordRepository borrowRecordRepository;
    private final FineCalculationService fineCalculationService;

    @Scheduled(cron = "0 5 0 * * *")
    @Transactional
    public void updateOverdueFinesDaily() {
        List<BorrowRecord> activeRecords = borrowRecordRepository.findByStatusIn(
                List.of(
                        BorrowStatus.BORROWED,
                        BorrowStatus.OVERDUE
                )
        );

        activeRecords.forEach(fineCalculationService::updateFineIfRequired);

        borrowRecordRepository.saveAll(activeRecords);

        log.info(
                "Daily overdue fine calculation completed. recordsProcessed={}",
                activeRecords.size()
        );
    }
}