package ib.finki.ukim.totp.services.interfaces.global;

public interface IEmailService {

    /**
     * Sends a registration email containing a token to the specified email address.
     *
     * @param email The recipient's email address.
     * @param token The token to include in the email.
     */
    void sendRegistrationEmail(String email, String token);

    /**
     * Generates an email containing a generated password and sends it to the specified email address.
     *
     * @param email The recipient's email address.
     * @param password The generated password to include in the email.
     */
    void generatePasswordEmail(String email, String password);
}
