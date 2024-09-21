package ib.finki.ukim.totp;

import ib.finki.ukim.totp.models.Customer;
import ib.finki.ukim.totp.models.Employee;
import ib.finki.ukim.totp.models.Transaction;
import ib.finki.ukim.totp.models.enums.TransactionStatus;
import ib.finki.ukim.totp.models.enums.TransactionType;
import ib.finki.ukim.totp.models.exceptions.authentication.*;
import ib.finki.ukim.totp.models.exceptions.transfer.InsufficientMoneyException;
import ib.finki.ukim.totp.models.exceptions.transfer.InvalidTransferNumberException;
import ib.finki.ukim.totp.models.exceptions.transfer.SameUserException;
import ib.finki.ukim.totp.models.exceptions.transfer.TimeExceededException;
import ib.finki.ukim.totp.repositories.CustomerRepository;
import ib.finki.ukim.totp.repositories.EmployeeRepository;
import ib.finki.ukim.totp.repositories.TransactionRepository;
import ib.finki.ukim.totp.services.interfaces.authentication.IAuthenticationService;
import ib.finki.ukim.totp.services.interfaces.authentication.ISaltingService;
import ib.finki.ukim.totp.services.interfaces.transfer.IOneTimePasswordService;
import ib.finki.ukim.totp.services.interfaces.transfer.ITransferService;
import jakarta.servlet.http.Cookie;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.Assert;

import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
public class TotpApplicationTests extends AbstractTestNGSpringContextTests {

    @Autowired
    CustomerRepository customerRepository;

    @Autowired
    EmployeeRepository employeeRepository;

    @Autowired
    TransactionRepository transactionRepository;

    @Autowired
    IOneTimePasswordService oneTimePasswordService;

    @Autowired
    ITransferService transferService;

    @Autowired
    ISaltingService saltingService;

    @Autowired
    IAuthenticationService authenticationService;

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @BeforeMethod
    void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @AfterMethod
    public void cleanup() {
        transactionRepository.deleteAll();
        customerRepository.deleteAll();
        customerRepository.deleteAll();
    }

    private MockHttpSession setupCustomerSession() throws Exception {
        String name = "Gjore";
        String surname = "Neco";
        String transactionNumber = "111";
        String password = "Test123!";
        String repeatPassword = "Test123!";
        String email = "populargjorgi@gmail.com";

        authenticationService.register(name, surname, transactionNumber, email, password, repeatPassword);

        Customer savedCustomer = customerRepository.findByTransactionNumber(transactionNumber);
        savedCustomer.setActivatedAccount(true);
        savedCustomer.setBalance(100);
        customerRepository.save(savedCustomer);

        Customer loggedInCustomer = authenticationService.login(transactionNumber, password);

        MockHttpSession session = new MockHttpSession();
        session.setAttribute("user", loggedInCustomer);

        String token = "token";
        session.setAttribute("token", token);

        Cookie tokenCookie = new Cookie("token", token);

        return session;
    }

    private MockHttpSession setupEmployeeSession() throws Exception {
        String name = "Gjore";
        String surname = "Neco";
        String password = "Test123!";
        String username = "Gjore";

        Employee employee = new Employee(name, surname, password, password.getBytes(), username);
        employeeRepository.save(employee);

        Employee loggedInEmployee = employeeRepository.findByUsername(username);

        MockHttpSession session = new MockHttpSession();
        session.setAttribute("user", loggedInEmployee);

        String token = "token";
        session.setAttribute("token", token);

        return session;
    }



    @Test(groups = "repository tests")
    void testCreateCustomerRepository() {
        Customer customer = new Customer("Gjore", "Neco", "111", "111", "111".getBytes(), "populargjorgi@gmail.com", "111", "111".getBytes());
        customerRepository.save(customer);

        Customer savedCustomer = customerRepository.findByTransactionNumber("111");
        Assert.assertNotNull(savedCustomer, "Saved customer should not be null");
    }

    @Test(groups = "repository tests")
    void testCreateEmployeeRepository(){
        Employee employee = new Employee("Gjore", "Neco", "111", "111".getBytes(), "Gjore");
        employeeRepository.save(employee);

        Employee savedEmployee = employeeRepository.findByUsername("Gjore");
        Assert.assertNotNull(savedEmployee, "Saved employee should not be null");
    }

    @Test(groups = "repository tests")
    void testCreatePendingTransaction(){
        Customer customer1 = new Customer("Gjore", "Neco", "111", "111", "111".getBytes(), "populargjorgi@gmail.com", "111", "111".getBytes());
        customer1.setBalance(100);
        Customer customer2 = new Customer("Gjore1", "Neco1", "222", "222", "222".getBytes(), "gjorginechovski@gmail.com", "222", "222".getBytes());

        customerRepository.save(customer1);
        customerRepository.save(customer2);

        Transaction transaction = new Transaction(customer1, customer2, 50, LocalDateTime.now(), TransactionType.TRANSFER);
        transactionRepository.save(transaction);

        Transaction savedTransaction = transactionRepository.findById(transaction.getId()).get();
        Assert.assertEquals(TransactionStatus.PENDING, savedTransaction.getStatus());
    }

    @Test(groups = "one time password service")
    void testVerifyTOTPTrue() {
        Customer customer1 = new Customer("Gjore", "Neco", "111", "111", "111".getBytes(), "populargjorgi@gmail.com", "111", "111".getBytes());
        customerRepository.save(customer1);

        Customer savedCustomer = customerRepository.findByTransactionNumber(customer1.getTransactionNumber());

        String otp = oneTimePasswordService.generateTOTP(savedCustomer.getSecretKey(), LocalDateTime.now());
        Assert.assertTrue(oneTimePasswordService.verifyTOTP(savedCustomer.getSecretKey(), otp, LocalDateTime.now()), "OTP should be valid");
    }

    @Test(groups = "one time password service")
    void testVerifyTOTPFalse() {
        Customer customer1 = new Customer("Gjore", "Neco", "111", "111", "111".getBytes(), "populargjorgi@gmail.com", "111", "111".getBytes());
        customerRepository.save(customer1);

        Customer savedCustomer = customerRepository.findByTransactionNumber(customer1.getTransactionNumber());

        String otp = oneTimePasswordService.generateTOTP(savedCustomer.getSecretKey(), LocalDateTime.now());
        Assert.assertFalse(oneTimePasswordService.verifyTOTP(savedCustomer.getSecretKey() +"1", otp, LocalDateTime.now()), "OTP should be valid");
    }

    @Test(groups = "transfer service")
    public void testCreateTransactionSuccessfully() {
        Customer customer1 = new Customer("Gjore", "Neco", "111", "111", "111".getBytes(), "populargjorgi@gmail.com", "111", "111".getBytes());
        customer1.setBalance(100);
        Customer customer2 = new Customer("Gjore1", "Neco1", "222", "222", "222".getBytes(), "gjorginechovski@gmail.com", "222", "222".getBytes());

        customerRepository.save(customer1);
        customerRepository.save(customer2);

        try {
            transferService.transfer(customer1, customer2.getTransactionNumber(), 50);
        } catch (Exception e) {
            Assert.fail("Exception should not have been thrown");
        }

        Assert.assertEquals(transactionRepository.findAll().size(), 1);
    }

    @Test(groups = "transfer service")
    void testCreateTransactionSameUserException() {
        Customer customer1 = new Customer("Gjore", "Neco", "111", "111", "111".getBytes(), "populargjorgi@gmail.com", "111", "111".getBytes());
        customer1.setBalance(100);

        customerRepository.save(customer1);

        try {
            transferService.transfer(customer1, customer1.getTransactionNumber(), 50);
        } catch (SameUserException e) {
            Assert.assertTrue(true);
            return;
        }
        catch (Exception e){
            Assert.fail("Exception should not have been caught here", e);
        }

        Assert.fail("SameUserException should have been thrown");
    }

    @Test(groups = "transfer service")
    void testCreateTransactionInvalidTransferNumber() {
        Customer customer1 = new Customer("Gjore", "Neco", "111", "111", "111".getBytes(), "populargjorgi@gmail.com", "111", "111".getBytes());
        customer1.setBalance(100);

        customerRepository.save(customer1);

        try {
            transferService.transfer(customer1, "222", 50);
        } catch (InvalidTransferNumberException e) {
            Assert.assertTrue(true);
            return;
        }
        catch (Exception e){
            Assert.fail("Exception should not have been caught here", e);
        }

        Assert.fail("InvalidTransferNumberException should have been thrown");
    }

    @Test(groups = "transfer service")
    public void testCreateTransactionInsufficientMoney() {
        Customer customer1 = new Customer("Gjore", "Neco", "111", "111", "111".getBytes(), "populargjorgi@gmail.com", "111", "111".getBytes());
        customer1.setBalance(100);
        Customer customer2 = new Customer("Gjore1", "Neco1", "222", "222", "222".getBytes(), "gjorginechovski@gmail.com", "222", "222".getBytes());

        customerRepository.save(customer1);
        customerRepository.save(customer2);

        try {
            transferService.transfer(customer1, customer2.getTransactionNumber(), 150);
        } catch (InsufficientMoneyException e) {
            Assert.assertTrue(true);
            return;
        }
        catch (Exception e){
            Assert.fail("Exception should not have been caught here", e);
        }

        Assert.fail("InsufficientMoneyException should have been thrown");
    }

    @Test(groups = "transfer service")
    public void testFinishTransferSuccessfully(){
        Customer customer1 = new Customer("Gjore", "Neco", "111", "111", "111".getBytes(), "populargjorgi@gmail.com", "111", "111".getBytes());
        customer1.setBalance(100);
        Customer customer2 = new Customer("Gjore1", "Neco1", "222", "222", "222".getBytes(), "gjorginechovski@gmail.com", "222", "222".getBytes());

        customerRepository.save(customer1);
        customerRepository.save(customer2);

        Transaction transaction = null;

        try {
            transaction = transferService.transfer(customer1, customer2.getTransactionNumber(), 50);
        } catch (Exception e) {
        }

        try{
            transferService.finishTransfer(transaction);
        }
        catch (Exception e){
            Assert.fail("Exception should not have been thrown");
            return;
        }

        Assert.assertEquals(transactionRepository.findById(transaction.getId()).get().getStatus(), TransactionStatus.COMPLETED);
    }

    @Test(groups = "transfer service")
    public void testFinishTransferTimeLimitExceeded(){
        Customer customer1 = new Customer("Gjore", "Neco", "111", "111", "111".getBytes(), "populargjorgi@gmail.com", "111", "111".getBytes());
        customer1.setBalance(100);
        Customer customer2 = new Customer("Gjore1", "Neco1", "222", "222", "222".getBytes(), "gjorginechovski@gmail.com", "222", "222".getBytes());

        customerRepository.save(customer1);
        customerRepository.save(customer2);

        Transaction transaction = null;

        try {
            transaction = transferService.transfer(customer1, customer2.getTransactionNumber(), 50);
        } catch (Exception e) {
        }

        transaction.setTimestamp(transaction.getTimestamp().minusSeconds(31));
        transactionRepository.save(transaction);

        try{
            transferService.finishTransfer(transaction);
        }
        catch (TimeExceededException e){
            Assert.assertTrue(true);
            return;
        }

        Assert.fail("TimeExceededException should have been thrown");
    }

    //TODO:
//    @Test(groups = "transfer service")
//    public void verifySuccess(){
//
//    }

    @Test(groups = "transfer service")
    public void withDrawInitiated(){
        Customer customer1 = new Customer("Gjore", "Neco", "111", "111", "111".getBytes(), "populargjorgi@gmail.com", "111", "111".getBytes());
        customerRepository.save(customer1);

        transferService.withDraw(customer1.getTransactionNumber(), 100);
        Transaction savedTransaction = transactionRepository.findAll().get(0);

        Assert.assertEquals(savedTransaction.getStatus(), TransactionStatus.PENDING);
        Assert.assertEquals(savedTransaction.getTransactionType(), TransactionType.WITHDRAWAL);
    }

    //TODO:
//    @Test(groups = "transfer service")
//    public void verifyWithdrawal(){
//
//    }

    @Test(groups = "transfer service")
    public void deposit(){
        Customer customer1 = new Customer("Gjore", "Neco", "111", "111", "111".getBytes(), "populargjorgi@gmail.com", "111", "111".getBytes());
        customerRepository.save(customer1);

        transferService.deposit(customer1.getTransactionNumber(), 100);
        Transaction savedTransaction = transactionRepository.findAll().get(0);
        Customer savedCustomer = customerRepository.findByTransactionNumber("111");

        Assert.assertEquals(savedTransaction.getStatus(), TransactionStatus.COMPLETED);
        Assert.assertEquals(savedTransaction.getTransactionType(), TransactionType.DEPOSIT);
        Assert.assertEquals(savedCustomer.getBalance(), 100);
    }

    @Test(groups = "salting service")
    public void testHashPassword(){
        String password = "password";
        byte[] salt = saltingService.generateSalt();
        String hashedPassword = saltingService.hash(password, salt);

        Assert.assertNotEquals(password, hashedPassword);
    }

    @Test(groups = "salting service")
    public void testVerifyPassword(){
        String password = "password";
        byte[] salt = saltingService.generateSalt();
        String hashedPassword = saltingService.hash(password, salt);

        Assert.assertTrue(saltingService.validate(password, hashedPassword, salt));
    }

    @Test(groups = "authentication service")
    public void testRegisterSuccessfully(){
        String name = "Gjore";
        String surname = "Neco";
        String transactionNumber = "111";
        String password = "Test123!";
        String repeatPassword = "Test123!";
        String email = "populargjorgi@gmail.com";

        try {
            authenticationService.register(name, surname, transactionNumber, email, password, repeatPassword);
        } catch (Exception e){
            Assert.fail("Shouldn't throw and exception");
            return;
        }

        Customer savedCustomer = customerRepository.findByTransactionNumber(transactionNumber);
        Assert.assertNotNull(savedCustomer);
    }

    @SneakyThrows
    @Test(groups = "authentication service")
    public void testRegisterPasswordsDontMatch(){
        String name = "Gjore";
        String surname = "Neco";
        String transactionNumber = "111";
        String password = "Test123!";
        String repeatPassword = "Test123";
        String email = "populargjorgi@gmail.com";

        try {
            authenticationService.register(name, surname, transactionNumber, email, password, repeatPassword);
        } catch (PasswordsDontMatchException e){
            Assert.assertTrue(true);
            return;
        }

        Assert.fail("Should throw an exception");
    }

    @SneakyThrows
    @Test(groups = "authentication service")
    public void testRegisterPasswordsNotStrongEnough(){
        String name = "Gjore";
        String surname = "Neco";
        String transactionNumber = "111";
        String password = "111111111";
        String repeatPassword = "111111111";
        String email = "populargjorgi@gmail.com";

        try {
            authenticationService.register(name, surname, transactionNumber, email, password, repeatPassword);
        } catch (PasswordNotMatchingRequirementsException e){
            Assert.assertTrue(true);
            return;
        }

        Assert.fail("Should throw an exception");
    }

    @SneakyThrows
    @Test(groups = "authentication service")
    public void testRegisterEmailExists(){
        String name = "Gjore";
        String surname = "Neco";
        String transactionNumber = "111";
        String password = "Test123!";
        String repeatPassword = "Test123!";
        String email = "populargjorgi@gmail.com";

        try {
            authenticationService.register(name, surname, transactionNumber, email, password, repeatPassword);
            authenticationService.register(name, surname, "222", email, password, repeatPassword);
        } catch (EmailExistsException e){
            Assert.assertTrue(true);
            return;
        }

        Assert.fail("Should throw an exception");
    }

    @SneakyThrows
    @Test(groups = "authentication service")
    public void testRegisterTransactionNumberExists(){
        String name = "Gjore";
        String surname = "Neco";
        String transactionNumber = "111";
        String password = "Test123!";
        String repeatPassword = "Test123!";
        String email = "populargjorgi@gmail.com";

        try {
            authenticationService.register(name, surname, transactionNumber, email, password, repeatPassword);
            authenticationService.register(name, surname, transactionNumber, "randomEmail", password, repeatPassword);
        } catch (TransactionNumberExistsExceptions e){
            Assert.assertTrue(true);
            return;
        }

        Assert.fail("Should throw an exception");
    }

    @SneakyThrows
    @Test(groups = "authentication service")
    public void testLoginSuccessfully() {
        String name = "Gjore";
        String surname = "Neco";
        String transactionNumber = "111";
        String password = "Test123!";
        String repeatPassword = "Test123!";
        String email = "populargjorgi@gmail.com";


        authenticationService.register(name, surname, transactionNumber, email, password, repeatPassword);


        Customer savedCustomer = customerRepository.findByTransactionNumber(transactionNumber);
        savedCustomer.setActivatedAccount(true);
        customerRepository.save(savedCustomer);

        savedCustomer = null;

        try {
            savedCustomer = authenticationService.login(transactionNumber, password);
        }
        catch (Exception e){
            System.out.println(e.getMessage());
            Assert.fail("Shouldn't throw an exception");
            return;
        }

        Assert.assertNotNull(savedCustomer);
    }

    @SneakyThrows
    @Test(groups = "authentication service")
    public void testLoginAccountNotActivated() {
        String name = "Gjore";
        String surname = "Neco";
        String transactionNumber = "111";
        String password = "Test123!";
        String repeatPassword = "Test123!";
        String email = "populargjorgi@gmail.com";

        authenticationService.register(name, surname, transactionNumber, email, password, repeatPassword);

        try {
            authenticationService.login(transactionNumber, password);
        }
        catch (AccountNotActivatedException e){
            Assert.assertTrue(true);
            return;
        }
        catch (Exception e){

        }

        Assert.fail("Shouldn't throw an exception");
    }
    @SneakyThrows
    @Test(groups = "authentication service")
    public void testLoginInvalidCredentials() {
        String name = "Gjore";
        String surname = "Neco";
        String transactionNumber = "111";
        String password = "Test123!";
        String repeatPassword = "Test123!";
        String email = "populargjorgi@gmail.com";

        authenticationService.register(name, surname, transactionNumber, email, password, repeatPassword);

        Customer savedCustomer = customerRepository.findByTransactionNumber(transactionNumber);
        savedCustomer.setActivatedAccount(true);
        customerRepository.save(savedCustomer);

        try {
            authenticationService.login(transactionNumber, "111");
        }
        catch (InvalidCredentialsException e){
            Assert.assertTrue(true);
            return;
        }
        catch (Exception e){
            System.out.println(e.getMessage());
        }

        Assert.fail("Shouldn't throw an exception");
    }

    @SneakyThrows
    @Test(groups = "authentication service")
    public void confirmRegisterTokenSuccessfully(){
        String name = "Gjore";
        String surname = "Neco";
        String transactionNumber = "111";
        String password = "Test123!";
        String repeatPassword = "Test123!";
        String email = "populargjorgi@gmail.com";

        authenticationService.register(name, surname, transactionNumber, email, password, repeatPassword);

        Customer savedCustomer = customerRepository.findByTransactionNumber(transactionNumber);
        String token = savedCustomer.getConfirmationToken();

        boolean validation = authenticationService.confirmRegisterToken(token);

        Assert.assertTrue(validation);
    }

    @SneakyThrows
    @Test(groups = "authentication service")
    public void confirmRegisterTokenFail(){
        String name = "Gjore";
        String surname = "Neco";
        String transactionNumber = "111";
        String password = "Test123!";
        String repeatPassword = "Test123!";
        String email = "populargjorgi@gmail.com";

        authenticationService.register(name, surname, transactionNumber, email, password, repeatPassword);

        boolean validation = authenticationService.confirmRegisterToken("111");

        Assert.assertFalse(validation);
    }

    @Test(groups = "authentication controller")
    void testLoginSuccess() throws Exception {
        String name = "Gjore";
        String surname = "Neco";
        String transactionNumber = "111";
        String password = "Test123!";
        String repeatPassword = "Test123!";
        String email = "populargjorgi@gmail.com";

        authenticationService.register(name, surname, transactionNumber, email, password, repeatPassword);

        Customer savedCustomer = customerRepository.findByTransactionNumber(transactionNumber);
        savedCustomer.setActivatedAccount(true);
        customerRepository.save(savedCustomer);

        mockMvc.perform(post("/login")
                        .param("transactionNumber", transactionNumber)
                        .param("password", password)
                        .session(new MockHttpSession())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/home"))
                .andExpect(cookie().exists("token"))
                .andExpect(model().attributeDoesNotExist("error"))
                .andDo(print());
    }

    @Test
    void testLoginInvalidCredentialsController() throws Exception {
        mockMvc.perform(post("/login")
                        .param("transactionNumber", "111")
                        .param("password", "wrongpassword")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andExpect(view().name("authentication/login"))
                .andExpect(model().attributeExists("error"))
                .andExpect(model().attribute("error", "The transaction number or the password is wrong!"))
                .andDo(print());
    }

    @Test(groups = "authentication controller")
    void testLoginAccountNotActivatedController() throws Exception {
        String name = "Gjore";
        String surname = "Neco";
        String transactionNumber = "111";
        String password = "Test123!";
        String repeatPassword = "Test123!";
        String email = "populargjorgi@gmail.com";

        authenticationService.register(name, surname, transactionNumber, email, password, repeatPassword);

        mockMvc.perform(post("/login")
                        .param("transactionNumber", transactionNumber)
                        .param("password", password)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andExpect(view().name("authentication/login"))
                .andExpect(model().attributeExists("error"))
                .andExpect(model().attribute("error", "The account is not activated!"))
                .andDo(print());
    }

    @Test(groups = "authentication controller")
    void testRegisterWithExistingEmail() throws Exception {
        String name = "Gjore";
        String surname = "Neco";
        String transactionNumber = "111";
        String email = "populargjorgi@gmail.com";
        String password = "Test123!";
        String repeatPassword = "Test123!";

        authenticationService.register(name, surname, transactionNumber, email, password, repeatPassword);

        mockMvc.perform(post("/register")
                        .param("name", name)
                        .param("surname", surname)
                        .param("transactionNumber", "222")
                        .param("email", email)
                        .param("password", password)
                        .param("repeatPassword", repeatPassword)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andExpect(view().name("authentication/register"))
                .andExpect(model().attributeExists("error"))
                .andExpect(model().attribute("error", "The entered email already has an account here!"))
                .andDo(print());
    }

    @Test(groups = "authentication controller")
    void testRegisterWithPasswordsDontMatch() throws Exception {
        String name = "Gjore";
        String surname = "Neco";
        String transactionNumber = "333";
        String email = "populargjorgi@gmail.com";
        String password = "Test123!";
        String repeatPassword = "DifferentPassword!";

        mockMvc.perform(post("/register")
                        .param("name", name)
                        .param("surname", surname)
                        .param("transactionNumber", transactionNumber)
                        .param("email", email)
                        .param("password", password)
                        .param("repeatPassword", repeatPassword)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andExpect(view().name("authentication/register"))
                .andExpect(model().attributeExists("error"))
                .andExpect(model().attribute("error", "The passwords don't match!"))
                .andDo(print());
    }

    @Test(groups = "authentication controller")
    void testRegisterWithExistingTransactionNumber() throws Exception {
        String name = "Gjore";
        String surname = "Neco";
        String transactionNumber = "111";
        String email = "populargjorgi@gmail.com";
        String password = "Test123!";
        String repeatPassword = "Test123!";

        authenticationService.register(name, surname, transactionNumber, email, password, repeatPassword);

        mockMvc.perform(post("/register")
                        .param("name", name)
                        .param("surname", surname)
                        .param("transactionNumber", transactionNumber)
                        .param("email", "different@gmail.com")
                        .param("password", password)
                        .param("repeatPassword", repeatPassword)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andExpect(view().name("authentication/register"))
                .andExpect(model().attributeExists("error"))
                .andExpect(model().attribute("error", "An email with that transaction number already exists! Please log in with that email!"))
                .andDo(print());
    }

    @Test(groups = "authentication controller")
    void testRegisterWithWeakPassword() throws Exception {
        String name = "Gjore";
        String surname = "Neco";
        String transactionNumber = "222";
        String email = "populargjorgi@gmail.com";
        String password = "weak";
        String repeatPassword = "weak";

        mockMvc.perform(post("/register")
                        .param("name", name)
                        .param("surname", surname)
                        .param("transactionNumber", transactionNumber)
                        .param("email", email)
                        .param("password", password)
                        .param("repeatPassword", repeatPassword)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andExpect(view().name("authentication/register"))
                .andExpect(model().attributeExists("error"))
                .andExpect(model().attribute("error", "Your password must contain at least 8 characters, one number, one capital letter and one special character"))
                .andDo(print());
    }

    @Test(groups = "authentication controller")
    void testSuccessfulRegistration() throws Exception {
        String name = "Gjore";
        String surname = "Neco";
        String transactionNumber = "333";
        String email = "populargjorgi@gmail.com";
        String password = "StrongPass1!";
        String repeatPassword = "StrongPass1!";

        mockMvc.perform(post("/register")
                        .param("name", name)
                        .param("surname", surname)
                        .param("transactionNumber", transactionNumber)
                        .param("email", email)
                        .param("password", password)
                        .param("repeatPassword", repeatPassword)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andExpect(view().name("authentication/showAuthMessage"))
                .andExpect(model().attributeExists("message"))
                .andExpect(model().attribute("message", "An email to confirm your account has been sent to you!"))
                .andDo(print());
    }

    @Test(groups = "authentication controller")
    void testConfirmEmailWithValidToken() throws Exception {
        String name = "Gjore";
        String surname = "Neco";
        String transactionNumber = "111";
        String password = "Test123!";
        String repeatPassword = "Test123!";
        String email = "populargjorgi@gmail.com";

        authenticationService.register(name, surname, transactionNumber, email, password, repeatPassword);
        Customer savedCustomer = customerRepository.findByTransactionNumber(transactionNumber);
        String token = savedCustomer.getConfirmationToken();

        mockMvc.perform(get("/confirm")
                        .param("token", token))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"))
                .andExpect(model().attributeDoesNotExist("error"))
                .andDo(print());
    }

    @Test(groups = "authentication controller")
    public void testLogout() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("user", "testUser");
        session.setAttribute("token", "testToken");

        Cookie tokenCookie = new Cookie("token", "testToken");

        MvcResult result = mockMvc.perform(post("/logout")
                        .session(session)
                        .cookie(tokenCookie))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"))
                .andExpect(request().sessionAttributeDoesNotExist("user"))
                .andExpect(request().sessionAttributeDoesNotExist("token"))
                .andDo(print())
                .andReturn();

        Cookie[] cookies = result.getResponse().getCookies();
        boolean tokenCookieRemoved = true;
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("token".equals(cookie.getName()) && cookie.getMaxAge() > 0) {
                    tokenCookieRemoved = false;
                    break;
                }
            }
        }

        Assert.assertTrue(tokenCookieRemoved, "Token cookie should be removed");
    }

    @Test
    public void testGetHome() throws Exception {
        MockHttpSession session = setupCustomerSession();

        String token = (String) session.getAttribute("token");
        Cookie tokenCookie = new Cookie("token", token);

        mockMvc.perform(get("/home")
                        .session(session)
                        .cookie(tokenCookie))
                .andExpect(status().isOk())
                .andExpect(view().name("master"))
                .andExpect(model().attributeExists("template"))
                .andExpect(model().attribute("template", "transactions/home"))
                .andDo(print());
    }


    @Test
    public void testGetTransfer() throws Exception {
        MockHttpSession session = setupCustomerSession();

        String token = (String) session.getAttribute("token");
        Cookie tokenCookie = new Cookie("token", token);

        mockMvc.perform(get("/transfer")
                        .session(session)
                        .cookie(tokenCookie))
                .andExpect(status().isOk())
                .andExpect(view().name("master"))
                .andExpect(model().attributeExists("template"))
                .andExpect(model().attribute("template", "transactions/transfer"))
                .andDo(print());
    }

    @Test(groups = "transfer controller")
    public void testTransferSuccess() throws Exception {
        MockHttpSession session = setupCustomerSession();
        Customer customer2 = new Customer("Gjore1", "Neco1", "222", "222", "222".getBytes(), "gjorginechovski@gmail.com", "222", "222".getBytes());
        customerRepository.save(customer2);

        String token = (String) session.getAttribute("token");
        Cookie tokenCookie = new Cookie("token", token);

        String transferNumber = "222";
        double amount = 50.0;

        mockMvc.perform(post("/transfer")
                        .session(session)
                        .cookie(tokenCookie)
                        .param("transferNumber", transferNumber)
                        .param("amount", String.valueOf(amount)))
                .andExpect(status().isOk())
                .andExpect(view().name("master"))
                .andExpect(model().attributeExists("template"))
                .andExpect(model().attribute("template", "transactions/transferPasswordInput"))
                .andExpect(model().attribute("transferNumber", transferNumber))
                .andExpect(model().attribute("amount", amount))
                .andExpect(model().attributeExists("transactionId"))
                .andDo(print());
    }

    @Test(groups = "transfer controller")
    public void testTransferInsufficientMoney() throws Exception {
        MockHttpSession session = setupCustomerSession();

        Customer customer2 = new Customer("Gjore1", "Neco1", "222", "222", "222".getBytes(), "gjorginechovski@gmail.com", "222", "222".getBytes());
        customerRepository.save(customer2);

        String token = (String) session.getAttribute("token");
        Cookie tokenCookie = new Cookie("token", token);

        String transferNumber = "222";
        double amount = 200;

        mockMvc.perform(post("/transfer")
                        .session(session)
                        .cookie(tokenCookie)
                        .param("transferNumber", transferNumber)
                        .param("amount", String.valueOf(amount)))
                .andExpect(status().isOk())
                .andExpect(view().name("master"))
                .andExpect(model().attribute("template", "transactions/transfer"))
                .andExpect(model().attribute("error", "You do not have sufficient money in your bank account to complete that transfer!"))
                .andDo(print());
    }

    @Test(groups = "transfer controller")
    public void testTransferInvalidTransferNumber() throws Exception {
        MockHttpSession session = setupCustomerSession();

        String token = (String) session.getAttribute("token");
        Cookie tokenCookie = new Cookie("token", token);

        String invalidTransferNumber = "999";
        double amount = 50.0;

        mockMvc.perform(post("/transfer")
                        .session(session)
                        .cookie(tokenCookie)
                        .param("transferNumber", invalidTransferNumber)
                        .param("amount", String.valueOf(amount)))
                .andExpect(status().isOk())
                .andExpect(view().name("master"))
                .andExpect(model().attribute("template", "transactions/transfer"))
                .andExpect(model().attribute("error", "Please enter a valid user's transfer number!"))
                .andDo(print());
    }

    @Test(groups = "transfer controller")
    public void testTransferNegativeAmount() throws Exception {
        MockHttpSession session = setupCustomerSession();

        Customer customer2 = new Customer("Gjore1", "Neco1", "222", "222", "222".getBytes(), "gjorginechovski@gmail.com", "222", "222".getBytes());
        customerRepository.save(customer2);

        String token = (String) session.getAttribute("token");
        Cookie tokenCookie = new Cookie("token", token);

        String transferNumber = "222";
        double negativeAmount = -50.0;

        mockMvc.perform(post("/transfer")
                        .session(session)
                        .cookie(tokenCookie)
                        .param("transferNumber", transferNumber)
                        .param("amount", String.valueOf(negativeAmount)))
                .andExpect(status().isOk())
                .andExpect(view().name("master"))
                .andExpect(model().attribute("template", "transactions/transfer"))
                .andExpect(model().attribute("error", "Please enter a positive amount!"))
                .andDo(print());
    }

    @Test(groups = "transfer controller")
    public void testTransferSameUserException() throws Exception {
        MockHttpSession session = setupCustomerSession();

        String token = (String) session.getAttribute("token");
        Cookie tokenCookie = new Cookie("token", token);

        String sameTransferNumber = "111";
        double amount = 50.0;

        mockMvc.perform(post("/transfer")
                        .session(session)
                        .cookie(tokenCookie)
                        .param("transferNumber", sameTransferNumber)
                        .param("amount", String.valueOf(amount)))
                .andExpect(status().isOk())
                .andExpect(view().name("master"))
                .andExpect(model().attribute("template", "transactions/transfer"))
                .andExpect(model().attribute("error", "You cannot transfer money to yourself!"))
                .andDo(print());
    }

    @Test(groups = "transaction controller")
    public void testGetListOfTransactions() throws Exception {
        MockHttpSession session = setupCustomerSession();

        String token = (String) session.getAttribute("token");
        Cookie tokenCookie = new Cookie("token", token);

        Customer customer = customerRepository.findByTransactionNumber("111");

        Transaction sentTransaction = new Transaction();
        sentTransaction.setTransactionType(TransactionType.TRANSFER);
        Transaction receivedTransaction = new Transaction();
        receivedTransaction.setTransactionType(TransactionType.DEPOSIT);

        customer.setSentTransactions(List.of(sentTransaction));
        customer.setReceivedTransactions(List.of(receivedTransaction));

        transactionRepository.save(sentTransaction);
        transactionRepository.save(receivedTransaction);
        customerRepository.save(customer);

        mockMvc.perform(get("/transactionList")
                        .session(session)
                        .cookie(tokenCookie)
                )
                .andExpect(status().isOk())
                .andExpect(view().name("master"))
                .andExpect(model().attributeExists("sentTransactions"))
                .andExpect(model().attributeExists("receivedTransactions"))
                .andExpect(model().attribute("template", "transactions/transactionList"))
                .andDo(print());
    }

    @Test(groups = "transaction controller")
    public void testGetWithdraw() throws Exception {
        MockHttpSession session = setupEmployeeSession();


        String token = (String) session.getAttribute("token");
        Cookie tokenCookie = new Cookie("token", token);

        mockMvc.perform(get("/withdraw")
                        .session(session)
                        .cookie(tokenCookie))
                .andExpect(status().isOk())
                .andExpect(view().name("master"))
                .andExpect(model().attributeExists("template"))
                .andExpect(model().attribute("template", "transactions/withdraw"))
                .andDo(print());
    }

    @Test(groups = "transaction controller")
    public void testGetWithdrawAsEmployee() throws Exception {
        MockHttpSession session = setupCustomerSession();

        String token = (String) session.getAttribute("token");
        Cookie tokenCookie = new Cookie("token", token);

        mockMvc.perform(get("/withdraw")
                        .session(session)
                        .cookie(tokenCookie))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("403"))
                .andDo(print());
    }

    @Test(groups = "transaction controller")
    public void testWithdrawSuccess() throws Exception {
        MockHttpSession session = setupEmployeeSession();

        String token = (String) session.getAttribute("token");
        Cookie tokenCookie = new Cookie("token", token);

        Customer customer1 = new Customer("Gjore", "Neco", "111", "111", "111".getBytes(), "populargjorgi@gmail.com", "111", "111".getBytes());
        customerRepository.save(customer1);

        String transferNumber = "111";
        double amount = 50.0;

        mockMvc.perform(post("/withdraw")
                        .session(session)
                        .cookie(tokenCookie)
                        .param("transactionNumber", transferNumber)
                        .param("amount", String.valueOf(amount)))
                .andExpect(status().isOk())
                .andExpect(view().name("master"))
                .andExpect(model().attributeExists("transferNumber"))
                .andExpect(model().attribute("transferNumber", transferNumber))
                .andExpect(model().attributeExists("amount"))
                .andExpect(model().attribute("amount", amount))
                .andExpect(model().attributeExists("transactionId"))
                .andExpect(model().attribute("template", "transactions/withdrawPasswordInput"))
                .andDo(print());
    }

    @Test(groups = "transaction controller")
    public void testGetDeposit() throws Exception {
        MockHttpSession session = setupEmployeeSession();
        String token = (String) session.getAttribute("token");
        Cookie tokenCookie = new Cookie("token", token);

        mockMvc.perform(get("/deposit")
                        .session(session)
                        .cookie(tokenCookie))
                .andExpect(status().isOk())
                .andExpect(view().name("master"))
                .andExpect(model().attributeExists("template"))
                .andExpect(model().attribute("template", "transactions/deposit"))
                .andDo(print());
    }

    @Test(groups = "transaction controller")
    public void testDepositSuccess() throws Exception {
        MockHttpSession session = setupEmployeeSession();
        String token = (String) session.getAttribute("token");
        Cookie tokenCookie = new Cookie("token", token);

        Customer customer1 = new Customer("Gjore", "Neco", "111", "111", "111".getBytes(), "populargjorgi@gmail.com", "111", "111".getBytes());
        customerRepository.save(customer1);

        String transferNumber = "111";
        double amount = 100.0;

        mockMvc.perform(post("/deposit")
                        .session(session)
                        .cookie(tokenCookie)
                        .param("transactionNumber", transferNumber)
                        .param("amount", String.valueOf(amount)))
                .andExpect(status().isOk())
                .andExpect(view().name("master"))
                .andExpect(model().attribute("template", "transactions/showMessage"))
                .andExpect(model().attribute("message", "Transaction successful!"))
                .andDo(print());

        Assert.assertEquals(customerRepository.findByTransactionNumber("111").getBalance(), 100.0);
    }


}