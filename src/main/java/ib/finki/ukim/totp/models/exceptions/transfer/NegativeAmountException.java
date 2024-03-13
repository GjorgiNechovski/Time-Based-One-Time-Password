package ib.finki.ukim.totp.models.exceptions.transfer;

public class NegativeAmountException extends Exception{
    public NegativeAmountException() {
        super("Please enter a positive amount!");
    }
}
