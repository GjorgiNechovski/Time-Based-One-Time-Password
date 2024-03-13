package ib.finki.ukim.totp.services.implementation.transfer;

import ib.finki.ukim.totp.models.Customer;
import ib.finki.ukim.totp.models.Transaction;
import ib.finki.ukim.totp.models.enums.TransactionStatus;
import ib.finki.ukim.totp.models.enums.TransactionType;
import ib.finki.ukim.totp.models.exceptions.transfer.*;
import ib.finki.ukim.totp.repositories.CustomerRepository;
import ib.finki.ukim.totp.repositories.TransactionRepository;
import ib.finki.ukim.totp.services.interfaces.global.IEmailService;
import ib.finki.ukim.totp.services.interfaces.transfer.IOneTimePasswordService;
import ib.finki.ukim.totp.services.interfaces.transfer.ITransferService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class TransferService implements ITransferService {
    private final CustomerRepository customerRepository;
    private final TransactionRepository transactionRepository;
    private final IOneTimePasswordService oneTimePasswordService;
    private final IEmailService emailService;

    public TransferService(CustomerRepository customerRepository, TransactionRepository transactionRepository, IOneTimePasswordService oneTimePasswordService, IEmailService emailService) {
        this.customerRepository = customerRepository;
        this.transactionRepository = transactionRepository;
        this.oneTimePasswordService = oneTimePasswordService;
        this.emailService = emailService;
    }

    @Override
    @Transactional
    public Transaction transfer(Customer fromUser, String to, double amount) throws InsufficientMoneyException, InvalidTransferNumberException, NegativeAmountException, SameUserException {
        Customer toUser = customerRepository.findByTransactionNumber(to);
        if (toUser == null){
            throw new InvalidTransferNumberException();
        }
        if (fromUser.getBalance() < amount){
            throw new InsufficientMoneyException();
        }
        if (amount <= 0){
            throw new NegativeAmountException();
        }
        if (fromUser.getTransactionNumber().equals(to)){
            throw new SameUserException();
        }

        LocalDateTime transactionTime = LocalDateTime.now();

        String oneTimePassword = oneTimePasswordService.generateTOTP(fromUser.getSecretKey(), transactionTime);

        Transaction transaction = new Transaction(fromUser, toUser, amount, transactionTime,  TransactionType.TRANSFER);
        emailService.generatePasswordEmail(fromUser.getEmail(), oneTimePassword);

        transactionRepository.save(transaction);

        return transaction;
    }

    @Override
    @Transactional
    public void finishTransfer(Transaction transaction) throws TimeExceededException {
        if (checkExpiredTransaction(transaction)){
            transaction.setTimestamp(null);
            transaction.setStatus(TransactionStatus.FAILED);
            transactionRepository.save(transaction);
            throw new TimeExceededException();
        }

        Customer fromUser = customerRepository.findById(transaction.getFromUser().getId());
        Customer toUser = customerRepository.findById(transaction.getToUser().getId());

        double moneyAmount = transaction.getAmount();

        double fromUserNewBalance = fromUser.getBalance() - moneyAmount;
        double toUserNewBalance = toUser.getBalance() + moneyAmount;

        fromUser.setBalance(fromUserNewBalance);
        toUser.setBalance(toUserNewBalance);
        transaction.setTimestamp(null);
        transaction.setStatus(TransactionStatus.COMPLETED);

        customerRepository.save(fromUser);
        customerRepository.save(toUser);
        transactionRepository.save(transaction);
    }

    @Override
    public boolean verify(Transaction transaction, Customer user, String password) {
        return this.oneTimePasswordService.verifyTOTP(user.getSecretKey(), password, transaction.getTimestamp());
    }


    @Override
    public Transaction getTransactionById(Long id) {
        return this.transactionRepository.findById(id).get();
    }

    @Override
    public Transaction withDraw(String to, double amount) {
        Customer customer = customerRepository.findByTransactionNumber(to);

        LocalDateTime transactionTime = LocalDateTime.now();
        String oneTimePassword = oneTimePasswordService.generateTOTP(customer.getSecretKey(), transactionTime);

        Transaction transaction = new Transaction(customer,customer,amount,transactionTime,TransactionType.WITHDRAWAL);

        emailService.generatePasswordEmail(customer.getEmail(), oneTimePassword);

        transactionRepository.save(transaction);
        return transaction;
    }

    @Override
    public void finishWithDraw(Transaction transaction) throws TimeExceededException {
        if (checkExpiredTransaction(transaction)){
            transaction.setStatus(TransactionStatus.FAILED);
            transaction.setTimestamp(null);
            transactionRepository.save(transaction);
            throw new TimeExceededException();
        }

        Customer fromUser = customerRepository.findById(transaction.getFromUser().getId());

        double moneyAmount = transaction.getAmount();

        double fromUserNewBalance = fromUser.getBalance() - moneyAmount;

        fromUser.setBalance(fromUserNewBalance);
        transaction.setTimestamp(null);
        transaction.setStatus(TransactionStatus.COMPLETED);

        customerRepository.save(fromUser);
        transactionRepository.save(transaction);
    }

    @Override
    public void deposit(String toUser, double amount) {
        Customer customer = customerRepository.findByTransactionNumber(toUser);
        double balance = customer.getBalance();

        Transaction transaction = new Transaction(customer,customer,amount,LocalDateTime.now(),TransactionType.DEPOSIT);
        transaction.setStatus(TransactionStatus.COMPLETED);

        customer.setBalance(balance + amount);
        customerRepository.save(customer);
        transactionRepository.save(transaction);
    }

    private boolean checkExpiredTransaction(Transaction transaction){
        LocalDateTime timestamp = transaction.getTimestamp();
        LocalDateTime now = LocalDateTime.now();

        long secondsPassed = ChronoUnit.SECONDS.between(timestamp, now);

        return secondsPassed > 30;
    }

}
