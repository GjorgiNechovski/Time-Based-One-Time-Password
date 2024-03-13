package ib.finki.ukim.totp.models.exceptions.transfer;

public class SameUserException extends Exception{
    public SameUserException() {
        super("You cannot transfer money to yourself!");
    }
}
