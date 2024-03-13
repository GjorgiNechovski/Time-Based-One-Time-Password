package ib.finki.ukim.totp.models.exceptions.authentication;

public class TransactionNumberExistsExceptions extends Exception{
    public TransactionNumberExistsExceptions() {
        super("An email with that transaction number already exists! Please log in with that email!");
    }
}
