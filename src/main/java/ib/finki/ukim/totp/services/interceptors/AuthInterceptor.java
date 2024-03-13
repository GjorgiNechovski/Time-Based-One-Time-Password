package ib.finki.ukim.totp.services.interceptors;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

public class AuthInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        Cookie[] cookies = request.getCookies();
        String token = (String) request.getSession().getAttribute("token");

        if(cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getValue().equals(token))
                    return true;
            }
        }

        response.sendRedirect("/login");
        return false;

    }
}
