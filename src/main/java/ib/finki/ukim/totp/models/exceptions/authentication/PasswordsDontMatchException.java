package ib.finki.ukim.totp.models.exceptions.authentication;

public class PasswordsDontMatchException extends Exception {
    public PasswordsDontMatchException() {
        super("The passwords don't match!");
    }
}
