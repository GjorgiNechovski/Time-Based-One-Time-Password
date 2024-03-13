package ib.finki.ukim.totp.models.exceptions.authentication;

public class PasswordNotMatchingRequirementsException extends Exception {
    public PasswordNotMatchingRequirementsException() {
        super("Your password must contain at least 8 characters, one number, one capital letter and one special character");
    }
}
