package ib.finki.ukim.totp.services.interceptors;

import ib.finki.ukim.totp.models.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

public class UserInterceptor implements HandlerInterceptor {
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        User user = (User) request.getSession().getAttribute("user");

        if(user != null && modelAndView != null){
            modelAndView.getModel().put("user", user);
        }

        HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
    }
}
