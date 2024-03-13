package ib.finki.ukim.totp.services.implementation.global;

import ib.finki.ukim.totp.services.interfaces.global.ICookieService;
import jakarta.servlet.http.Cookie;
import org.springframework.stereotype.Service;

@Service
public class CookieService implements ICookieService {
    @Override
    public Cookie generateLoggedCookie() {
        String token = "loggedIn";
        Cookie cookie = new Cookie("token", token);
        cookie.setMaxAge(360);
        cookie.setPath("/");

        return cookie;
    }

    @Override
    public Cookie removeTokenCookie(Cookie[] cookies) {
        for (Cookie cookie : cookies){
            if (cookie.getName().equals("token")){
                cookie.setMaxAge(0);
                return cookie;
            }
        }
        return null;
    }
}
