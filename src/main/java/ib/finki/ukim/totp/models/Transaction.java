package ib.finki.ukim.totp.models;

import ib.finki.ukim.totp.models.enums.TransactionStatus;
import ib.finki.ukim.totp.models.enums.TransactionType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@NoArgsConstructor
@Data
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "from_user")
    private Customer fromUser;

    @ManyToOne
    @JoinColumn(name = "to_user")
    private Customer toUser;

    private double amount;

    private LocalDateTime timestamp;

    @Enumerated(EnumType.STRING)
    private TransactionType transactionType;

    @Enumerated(EnumType.STRING)
    private TransactionStatus status;

    public Transaction(Customer fromUser, Customer toUser, double amount, LocalDateTime timestamp, TransactionType transactionType) {
        this.fromUser = fromUser;
        this.toUser = toUser;
        this.amount = amount;
        this.timestamp = timestamp;
        this.status = TransactionStatus.PENDING;
        this.transactionType = transactionType;
    }


}
