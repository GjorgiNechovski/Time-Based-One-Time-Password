package ib.finki.ukim.totp.services.implementation.authentication;

import ib.finki.ukim.totp.models.Customer;
import ib.finki.ukim.totp.models.Employee;
import ib.finki.ukim.totp.models.exceptions.authentication.*;
import ib.finki.ukim.totp.repositories.CustomerRepository;
import ib.finki.ukim.totp.repositories.EmployeeRepository;
import ib.finki.ukim.totp.services.interfaces.authentication.IAuthenticationService;
import ib.finki.ukim.totp.services.interfaces.global.IEmailService;
import ib.finki.ukim.totp.services.interfaces.authentication.ISaltingService;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class AuthenticationService implements IAuthenticationService {
    private final CustomerRepository customerRepository;
    private final EmployeeRepository employeeRepository;
    private final ISaltingService saltingService;
    private final IEmailService emailService;

    public AuthenticationService(CustomerRepository customerRepository, EmployeeRepository employeeRepository, ISaltingService saltingService, IEmailService emailService) {
        this.customerRepository = customerRepository;
        this.employeeRepository = employeeRepository;
        this.saltingService = saltingService;
        this.emailService = emailService;
    }

    @Override
    public Customer login(String transactionNumber, String password) throws AccountNotActivatedException, InvalidCredentialsException {
        Customer user = customerRepository.findByTransactionNumber(transactionNumber);
        if (user == null){
            throw new InvalidCredentialsException();
        }

        if (!user.isActivatedAccount()){
            throw new AccountNotActivatedException();
        }

        byte[] passwordSalt = user.getPasswordSalt();
        if (!saltingService.validate(password, user.getPassword(), passwordSalt)){
            throw new InvalidCredentialsException();
        }

        return user;
    }

    @Override
    public void register(String name, String surname, String transactionNumber, String email, String password, String repeatPassword) throws EmailExistsException, PasswordsDontMatchException, TransactionNumberExistsExceptions, PasswordNotMatchingRequirementsException {
        if(!checkRequirements(password)){
            throw new PasswordNotMatchingRequirementsException();
        }
        if (customerRepository.findByTransactionNumber(transactionNumber) != null){
            throw new TransactionNumberExistsExceptions();
        }
        if (customerRepository.findByEmail(email)!=null){
            throw new EmailExistsException();
        }
        if (!password.equals(repeatPassword)){
            throw new PasswordsDontMatchException();
        }

        byte[] passwordSalt = saltingService.generateSalt();
        String hashedPassword = saltingService.hash(password, passwordSalt);

        byte[] secretKeySalt = saltingService.generateSalt();
        String secretKey = generateSecretKey(secretKeySalt);

        Customer user = new Customer(name,surname,transactionNumber,hashedPassword,passwordSalt,email,secretKey,secretKeySalt);

        String emailConfirmationLink = generateSecretKey(saltingService.generateSalt());
        user.setConfirmationToken(emailConfirmationLink);
        emailService.sendRegistrationEmail(email, emailConfirmationLink);

        customerRepository.save(user);
    }

    @Override
    public boolean confirmRegisterToken(String token) {
        Customer user = customerRepository.findByConfirmationToken(token);
        if (user==null){
            return false;
        }
        user.setActivatedAccount(true);
        user.setConfirmationToken(null);
        customerRepository.save(user);
        return true;
    }

    @Override
    public void employeeRegister(String name, String surname, String username, String password, String repeatPassword) throws PasswordsDontMatchException {
        if (!password.equals(repeatPassword)){
            throw new PasswordsDontMatchException();
        }

        byte[] passwordSalt = saltingService.generateSalt();
        String hashedPassword = saltingService.hash(password, passwordSalt);

        Employee employee = new Employee(name,surname,hashedPassword,passwordSalt, username);

        employeeRepository.save(employee);
    }

    @Override
    public Employee employeeLogin(String username, String password) throws InvalidCredentialsException {
        Employee employee = employeeRepository.findByUsername(username);

        if (employee == null){
            throw new InvalidCredentialsException();
        }

        byte[] passwordSalt = employee.getPasswordSalt();
        if (!saltingService.validate(password, employee.getPassword(), passwordSalt)){
            throw new InvalidCredentialsException();
        }

        return employee;
    }

    private boolean checkRequirements(String password) {
        String specialChars = "~`!@#$%^&*()-_=+\\|[{]};:'\",<.>/?";
        char currentCharacter;
        boolean numberPresent = false;
        boolean upperCasePresent = false;
        boolean lowerCasePresent = false;
        boolean specialCharacterPresent = false;

        for (int i = 0; i < password.length(); i++) {
            currentCharacter = password.charAt(i);
            if (Character.isDigit(currentCharacter)) {
                numberPresent = true;
            } else if (Character.isUpperCase(currentCharacter)) {
                upperCasePresent = true;
            } else if (Character.isLowerCase(currentCharacter)) {
                lowerCasePresent = true;
            } else if (specialChars.contains(String.valueOf(currentCharacter))) {
                specialCharacterPresent = true;
            }
        }

        return numberPresent && upperCasePresent && lowerCasePresent && specialCharacterPresent;
    }

    private String generateSecretKey(byte[] salt){
        int leftLimit = 48;
        int rightLimit = 122;
        int targetStringLength = 10;
        Random random = new Random();

        String secretKeyRandomised =  random.ints(leftLimit, rightLimit + 1)
                .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
                .limit(targetStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();

        return saltingService.hash(secretKeyRandomised, salt);
    }
}
