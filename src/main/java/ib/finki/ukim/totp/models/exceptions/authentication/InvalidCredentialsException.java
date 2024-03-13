package ib.finki.ukim.totp.models.exceptions.authentication;

public class InvalidCredentialsException extends Exception {
    public InvalidCredentialsException() {
        super("The transaction number or the password is wrong!");
    }
}
