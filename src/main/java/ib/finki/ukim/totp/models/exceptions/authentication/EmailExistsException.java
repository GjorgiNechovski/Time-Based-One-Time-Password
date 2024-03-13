package ib.finki.ukim.totp.models.exceptions.authentication;

public class EmailExistsException extends Exception{
    public EmailExistsException() {
        super("The entered email already has an account here!");
    }
}
