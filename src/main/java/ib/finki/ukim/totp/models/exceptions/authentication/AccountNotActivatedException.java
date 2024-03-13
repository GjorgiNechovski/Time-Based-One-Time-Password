package ib.finki.ukim.totp.models.exceptions.authentication;

public class AccountNotActivatedException extends Exception{
    public AccountNotActivatedException() {
        super("The account is not activated!");
    }
}
