package ib.finki.ukim.totp.models.exceptions.transfer;

public class TimeExceededException extends Exception{
    public TimeExceededException() {
        super("The time exceeded for this transaction, please try making a new one!");
    }
}
