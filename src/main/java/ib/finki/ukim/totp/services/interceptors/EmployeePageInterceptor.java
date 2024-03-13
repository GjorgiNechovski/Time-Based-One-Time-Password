package ib.finki.ukim.totp.services.interceptors;

import ib.finki.ukim.totp.models.User;
import ib.finki.ukim.totp.models.enums.UserRole;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

public class EmployeePageInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        UserRole role = ((User) request.getSession().getAttribute("user")).getRole();

        if(role == UserRole.Employee){
            return true;
        }

        response.sendRedirect("403");
        return false;
    }
}
