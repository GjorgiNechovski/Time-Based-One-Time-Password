package ib.finki.ukim.totp.config;

import ib.finki.ukim.totp.models.Transaction;
import ib.finki.ukim.totp.models.enums.TransactionStatus;
import ib.finki.ukim.totp.repositories.TransactionRepository;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 *
 * A component that checks for any transaction that's older than 10 minutes
 * if it's older than 10 minutes it sets the transaction as failed and deletes the time it was generated
 * It activates every 10 minutes
 * */

@Component
@EnableScheduling
public class TransactionScheduler {
    public final TransactionRepository repository;

    public TransactionScheduler(TransactionRepository repository) {
        this.repository = repository;
    }

    @Scheduled(fixedRate = 300000)
    public void failExpiredTransactions(){
        List<Transaction> transactionList = repository.findByStatus(TransactionStatus.PENDING);

        transactionList.forEach(transaction -> {
            if (checkExpired(transaction)){
                transaction.setStatus(TransactionStatus.FAILED);
                transaction.setTimestamp(null);
                repository.save(transaction);
            }
        });
    }

    private boolean checkExpired(Transaction transaction){
        LocalDateTime timestamp = transaction.getTimestamp();
        LocalDateTime now = LocalDateTime.now();

        long secondsPassed = ChronoUnit.SECONDS.between(timestamp, now);

        return secondsPassed > 30;
    }
}
