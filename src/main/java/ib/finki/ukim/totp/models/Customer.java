package ib.finki.ukim.totp.models;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Data
@NoArgsConstructor
public class Customer extends User {
    private String transactionNumber;

    private String email;

    private double balance;

    private byte[] secretKeySalt;

    private String secretKey;

    private String confirmationToken;

    private boolean activatedAccount;

    @OneToMany(mappedBy = "fromUser", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private List<Transaction> sentTransactions;

    @OneToMany(mappedBy = "toUser", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private List<Transaction> receivedTransactions;

    public Customer(String name, String surname,String transactionNumber, String password, byte[] passwordSalt, String email,  String secretKey, byte[] secretKeySalt) {
        super(name, surname, password, passwordSalt);
        this.transactionNumber = transactionNumber;
        this.email = email;
        this.balance = 0;
        this.secretKeySalt = secretKeySalt;
        this.secretKey = secretKey;
    }
}