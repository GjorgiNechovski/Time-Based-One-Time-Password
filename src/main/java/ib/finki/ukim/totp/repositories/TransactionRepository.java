package ib.finki.ukim.totp.repositories;

import ib.finki.ukim.totp.models.Transaction;
import ib.finki.ukim.totp.models.enums.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByStatus(TransactionStatus transactionStatus);
}
