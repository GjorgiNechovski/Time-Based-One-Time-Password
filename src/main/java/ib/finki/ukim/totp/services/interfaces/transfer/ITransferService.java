package ib.finki.ukim.totp.services.interfaces.transfer;

import ib.finki.ukim.totp.models.Customer;
import ib.finki.ukim.totp.models.Transaction;
import ib.finki.ukim.totp.models.exceptions.transfer.*;

import java.util.List;

/**
 * A service interface for handling transfer transactions in the system.
 */
public interface ITransferService {

    /**
     * Transfers a specified amount from one customer to another.
     *
     * @param fromUser The customer initiating the transfer.
     * @param to The recipient of the transfer.
     * @param amount The amount to transfer.
     * @return The transaction object representing the transfer.
     * @throws InsufficientMoneyException Thrown if the initiating customer has insufficient funds.
     * @throws InvalidTransferNumberException Thrown if the transfer number is invalid.
     * @throws NegativeAmountException Thrown if the transfer amount is negative.
     * @throws SameUserException Thrown if the recipient is the same as the sender.
     */
    Transaction transfer(Customer fromUser, String to, double amount) throws InsufficientMoneyException, InvalidTransferNumberException, NegativeAmountException, SameUserException;

    /**
     * Completes a transfer transaction.
     *
     * @param transaction The transaction to be completed.
     * @throws TimeExceededException Thrown if the transaction completion time has exceeded.
     */
    void finishTransfer(Transaction transaction) throws TimeExceededException;

    /**
     * Verifies a transaction.
     *
     * @param transaction The transaction to verify.
     * @param user The user initiating the verification.
     * @param password The password of the user initiating the verification.
     * @return True if the verification is successful, false otherwise.
     */
    boolean verify(Transaction transaction, Customer user, String password);

    /**
     * Retrieves a transaction by its ID.
     *
     * @param id The ID of the transaction to retrieve.
     * @return The transaction object corresponding to the ID.
     */
    Transaction getTransactionById(Long id);

    /**
     * Initiates a withdrawal transaction.
     *
     * @param to The recipient of the withdrawal.
     * @param amount The amount to withdraw.
     * @return The transaction object representing the withdrawal.
     */
    Transaction withDraw(String to, double amount);

    /**
     * Completes a withdrawal transaction.
     *
     * @param transaction The withdrawal transaction to complete.
     * @throws TimeExceededException Thrown if the withdrawal transaction completion time has exceeded.
     */
    void finishWithDraw(Transaction transaction) throws TimeExceededException;

    /**
     * Initiates a deposit transaction.
     *
     * @param toUser The user to deposit money to.
     * @param amount The amount to deposit.
     */
    void deposit(String toUser, double amount);
}
