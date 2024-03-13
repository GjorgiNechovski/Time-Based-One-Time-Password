package ib.finki.ukim.totp.config;

import ib.finki.ukim.totp.services.interceptors.AuthInterceptor;
import ib.finki.ukim.totp.services.interceptors.CustomerPageInterceptor;
import ib.finki.ukim.totp.services.interceptors.EmployeePageInterceptor;
import ib.finki.ukim.totp.services.interceptors.UserInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class AuthenticationInterceptorsConfig implements WebMvcConfigurer {
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new AuthInterceptor())
                .addPathPatterns("/*")
                .excludePathPatterns(authUrls);

        registry.addInterceptor(new UserInterceptor())
                .addPathPatterns("/*")
                .excludePathPatterns(authUrls);

        registry.addInterceptor(new EmployeePageInterceptor())
                .addPathPatterns(employeeUrls)
                .excludePathPatterns(authUrls);

        registry.addInterceptor(new CustomerPageInterceptor())
                .addPathPatterns("/*")
                .excludePathPatterns(employeeUrls)
                .excludePathPatterns(authUrls);
    }

    List<String> authUrls = List.of("/login", "/register", "/confirm", "/logout", "/employee-login", "/employee-register");
    List<String> employeeUrls = List.of("/withdraw", "/withdrawPasswordInput", "/deposit");

}
