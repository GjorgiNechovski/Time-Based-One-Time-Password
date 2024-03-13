package ib.finki.ukim.totp.models.exceptions.transfer;

public class InvalidTransferNumberException extends Exception{
    public InvalidTransferNumberException() {
        super("Please enter a valid user's transfer number!");
    }
}
