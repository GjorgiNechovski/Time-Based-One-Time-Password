package ib.finki.ukim.totp.services.interfaces.global;

import jakarta.servlet.http.Cookie;

public interface ICookieService {

    /**
     * Generates a cookie to indicate that a user is logged in.
     *
     * @return The generated logged-in cookie.
     */
    Cookie generateLoggedCookie();

    /**
     * Removes a token cookie from the provided array of cookies.
     *
     * @param cookies The array of cookies from which the token cookie needs to be removed.
     * @return The removed token cookie.
     */
    Cookie removeTokenCookie(Cookie[] cookies);
}
