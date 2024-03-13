package ib.finki.ukim.totp.controllers;

import ib.finki.ukim.totp.models.Customer;
import ib.finki.ukim.totp.models.Transaction;
import ib.finki.ukim.totp.models.exceptions.transfer.*;
import ib.finki.ukim.totp.services.implementation.global.UserService;
import ib.finki.ukim.totp.services.interfaces.transfer.ITransferService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class TransactionController {
    private final ITransferService transferService;
    private final UserService userService;

    public TransactionController(ITransferService transferService, UserService userService) {
        this.transferService = transferService;
        this.userService = userService;
    }

    @GetMapping("/home")
    public String getHome(Model model){
        model.addAttribute("template", "transactions/home");
        return "master";
    }

    @GetMapping("/transfer")
    public String getTransfer(Model model){
        model.addAttribute("template", "transactions/transfer");
        return "master";
    }

    @PostMapping("/transfer")
    public String transfer(@RequestParam String transferNumber,
                           @RequestParam double amount,
                           Model model,
                           HttpSession session){
        Customer user = (Customer) session.getAttribute("user");
        Transaction transaction = null;
        try {
            transaction = this.transferService.transfer(user,transferNumber,amount);
        } catch (InsufficientMoneyException | InvalidTransferNumberException | NegativeAmountException |
                 SameUserException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("template", "transactions/transfer");

            return "master";
        }

        model.addAttribute("transferNumber", transferNumber);
        model.addAttribute("amount", amount);
        model.addAttribute("template","transactions/transferPasswordInput");
        model.addAttribute("transactionId", transaction.getId());
        return "master";
    }

    @PostMapping("/confirmTransactionPassword")
    public String finishTransaction(@RequestParam String password,
                                    @RequestParam Long transactionId,
                                    @RequestParam String transferNumber,
                                    @RequestParam String amount,
                                    HttpSession session,
                                    Model model){
        Customer user = (Customer) session.getAttribute("user");
        Transaction transaction = this.transferService.getTransactionById(transactionId);

        if(!this.transferService.verify(transaction,user,password)){
            model.addAttribute("error", "The password you entered is wrong please try again!");
            model.addAttribute("transactionId", transactionId);
            model.addAttribute("transferNumber", transferNumber);
            model.addAttribute("amount", amount);
            model.addAttribute("template","transactions/transferPasswordInput");
            return "master";
        }

        try {
            this.transferService.finishTransfer(transaction);

            Customer customer = userService.getCustomerById(user.getId());
            session.setAttribute("user", customer);
        } catch (TimeExceededException e) {
            model.addAttribute("template","transactions/showMessage");
            model.addAttribute("message", e.getMessage());
            return "master";
        }

        user = userService.getCustomerById(user.getId());
        session.setAttribute("user", user);

        model.addAttribute("template","transactions/showMessage");
        model.addAttribute("message", "Transaction successful!");
        return "master";
    }

    @GetMapping("/transactionList")
    public String getListOfTransactions(HttpSession session, Model model){
        Customer user = (Customer) session.getAttribute("user");

        List<Transaction> sentTransactions = user.getSentTransactions();
        List<Transaction> receivedTransactions = user.getReceivedTransactions();

        model.addAttribute("sentTransactions", sentTransactions);
        model.addAttribute("receivedTransactions", receivedTransactions);
        model.addAttribute("template", "transactions/transactionList");

        return "master";
    }

    @GetMapping("/withdraw")
    public String getWithdraw(Model model){
        model.addAttribute("template", "transactions/withdraw");
        return "master";
    }

    @PostMapping("/withdraw")
    public String withDraw(@RequestParam String transactionNumber,
                           @RequestParam double amount,
                           Model model){
        Transaction transaction = transferService.withDraw(transactionNumber, amount);

        model.addAttribute("transferNumber", transactionNumber);
        model.addAttribute("amount", amount);
        model.addAttribute("template","transactions/withdrawPasswordInput");
        model.addAttribute("transactionId", transaction.getId());
        return "master";
    }

    @PostMapping("/withdrawPasswordInput")
    public String getWithDrawPasswordInput(@RequestParam String password,
                                           @RequestParam Long transactionId,
                                           @RequestParam String transferNumber,
                                           @RequestParam String amount,
                                           Model model){
        Transaction transaction = this.transferService.getTransactionById(transactionId);

        Customer customer = transaction.getToUser();

        if (!transferService.verify(transaction,customer,password)){
            model.addAttribute("error", "The password you entered is wrong please try again!");
            model.addAttribute("transactionId", transactionId);
            model.addAttribute("transferNumber", transferNumber);
            model.addAttribute("amount", amount);
            model.addAttribute("template","transactions/withdrawPasswordInput");
            return "master";
        }

        try {
            transferService.finishWithDraw(transaction);
        } catch (TimeExceededException e) {
            model.addAttribute("template","transactions/showMessage");
            model.addAttribute("message", e.getMessage());
            return "master";
        }

        model.addAttribute("template","transactions/showMessage");
        model.addAttribute("message", "Transaction successful!");
        return "master";
    }

    @GetMapping("/deposit")
    public String getDeposit(Model model){
        model.addAttribute("template", "transactions/deposit");
        return "master";
    }

    @PostMapping("/deposit")
    public String deposit(@RequestParam String transactionNumber,
                          @RequestParam double amount,
                          Model model){
        transferService.deposit(transactionNumber, amount);

        model.addAttribute("template","transactions/showMessage");
        model.addAttribute("message", "Transaction successful!");
        return "master";
    }
}
