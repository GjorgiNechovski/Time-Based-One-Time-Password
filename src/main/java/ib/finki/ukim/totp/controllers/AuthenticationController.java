package ib.finki.ukim.totp.controllers;

import ib.finki.ukim.totp.models.Employee;
import ib.finki.ukim.totp.models.User;
import ib.finki.ukim.totp.models.exceptions.authentication.InvalidCredentialsException;
import ib.finki.ukim.totp.models.exceptions.authentication.PasswordsDontMatchException;
import ib.finki.ukim.totp.services.interfaces.authentication.IAuthenticationService;
import ib.finki.ukim.totp.services.interfaces.global.ICookieService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthenticationController {
    private final IAuthenticationService authenticationService;
    private final ICookieService cookieService;

    public AuthenticationController(IAuthenticationService authenticationService, ICookieService cookieService) {
        this.authenticationService = authenticationService;
        this.cookieService = cookieService;
    }

    @GetMapping("/login")
    public String getLogin(){
        return "authentication/login";
    }

    @GetMapping("/register")
    public String getRegister(){
        return "authentication/register";
    }

    @PostMapping("/login")
    public String login(@RequestParam String transactionNumber,
                        @RequestParam String password,
                        HttpSession httpSession,
                        HttpServletResponse response,
                        Model model){
        User user;
        try {
            user = authenticationService.login(transactionNumber,password);
        }
        catch (Exception e){
            model.addAttribute("error", e.getMessage());
            return "authentication/login";
        }

        Cookie token = cookieService.generateLoggedCookie();
        response.addCookie(token);
        httpSession.setAttribute("token", token.getValue());

        httpSession.setAttribute("user", user);
        return "redirect:/home";
    }

    @GetMapping("/employee-login")
    public String getEmployeeLogin(){
        return "authentication/employee-login";
    }

    @PostMapping("/employee-login")
    public String employeeLogin(@RequestParam String username,
                                @RequestParam String password,
                                HttpServletResponse response,
                                HttpSession httpSession,
                                Model model){
        Employee user = null;
        try {
            user = authenticationService.employeeLogin(username,password);
        } catch (InvalidCredentialsException e) {
            model.addAttribute("error", e.getMessage());
            return "authentication/employee-login";
        }

        Cookie token = cookieService.generateLoggedCookie();
        response.addCookie(token);
        httpSession.setAttribute("token", token.getValue());

        httpSession.setAttribute("user", user);
        return "redirect:/withdraw";
    }

    @GetMapping("/employee-register")
    public String getEmployeeRegistration(){
        return "authentication/employee-registration";
    }

    @PostMapping("/employee-register")
    public String employeeRegistration(@RequestParam String name,
                                       @RequestParam String surname,
                                       @RequestParam String username,
                                       @RequestParam String password,
                                       @RequestParam String repeatPassword,
                                       Model model){
        try {
            this.authenticationService.employeeRegister(name,surname,username,password,repeatPassword);
        } catch (PasswordsDontMatchException e) {
            model.addAttribute("error", e.getMessage());
            return "authentication/employee-registration";
        }
        return "redirect:/employee-login";
    }

    @PostMapping("/register")
    public String register(@RequestParam String name,
                           @RequestParam String surname,
                           @RequestParam String transactionNumber,
                           @RequestParam String email,
                           @RequestParam String password,
                           @RequestParam String repeatPassword,
                           Model model) {
        try {
            authenticationService.register(name, surname, transactionNumber, email, password, repeatPassword);
        } catch (Exception runtimeException) {
            model.addAttribute("error", runtimeException.getMessage());
            return "authentication/register";
        }

        model.addAttribute("message", "An email to confirm your account has been sent to you!");
        return "authentication/showAuthMessage";
    }

    @GetMapping("/confirm")
    public String confirmEmail(@RequestParam String token, Model model){
        if (!authenticationService.confirmRegisterToken(token)){
            model.addAttribute("error", "The token you provided does not exist! Please try again");
        }
        return "redirect:/login";
    }

    @PostMapping("/logout")
    public String logout(HttpSession httpSession,
                         HttpServletRequest request,
                         HttpServletResponse response){
        Cookie[] cookies = request.getCookies();
        Cookie token = cookieService.removeTokenCookie(cookies);
        response.addCookie(token);

        httpSession.removeAttribute("user");
        httpSession.removeAttribute("token");
        return "redirect:/login";
    }

    @GetMapping("403")
    public String get403(){
        return "error/403";
    }
}
