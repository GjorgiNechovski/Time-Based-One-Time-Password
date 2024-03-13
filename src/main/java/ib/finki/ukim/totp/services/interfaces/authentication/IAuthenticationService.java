package ib.finki.ukim.totp.services.interfaces.authentication;

import ib.finki.ukim.totp.models.Customer;
import ib.finki.ukim.totp.models.Employee;
import ib.finki.ukim.totp.models.exceptions.authentication.*;

public interface IAuthenticationService {
    /**
     * Allows a customer to log in using their transaction number and password.
     *
     * @param transactionNumber The unique transaction number associated with the customer.
     * @param password The password provided by the customer.
     * @return The logged-in customer object.
     * @throws AccountNotActivatedException Thrown if the customer's account is not activated.
     * @throws InvalidCredentialsException Thrown if the provided credentials are invalid.
     */
    Customer login(String transactionNumber, String password) throws AccountNotActivatedException, InvalidCredentialsException;
    /**
     * Registers a new customer with the provided information.
     *
     * @param name The customer's first name.
     * @param surname The customer's last name.
     * @param transactionNumber The unique transaction number to be associated with the customer.
     * @param email The customer's email address.
     * @param password The desired password for the new account.
     * @param repeatPassword The repeated password for confirmation.
     * @throws EmailExistsException Thrown if the provided email address already exists in the system.
     * @throws PasswordsDontMatchException Thrown if the provided passwords do not match.
     * @throws TransactionNumberExistsExceptions Thrown if the provided transaction number already exists in the system.
     */
    void register(String name, String surname, String transactionNumber, String email, String password, String repeatPassword) throws EmailExistsException, PasswordsDontMatchException, TransactionNumberExistsExceptions, PasswordNotMatchingRequirementsException;

    /**
     * Confirms the registration token sent to a new customer's email.
     *
     * @param token The registration token to confirm.
     * @return True if the token is confirmed successfully, false otherwise.
     */
    boolean confirmRegisterToken(String token);
    void employeeRegister(String name, String surname, String username, String password, String repeatPassword) throws PasswordsDontMatchException;
    Employee employeeLogin(String username, String password) throws InvalidCredentialsException;
}
