package ib.finki.ukim.totp.models.exceptions.transfer;

public class InsufficientMoneyException extends Exception{
    public InsufficientMoneyException() {
        super("You do not have sufficient money in your bank account to complete that transfer!");
    }
}
