package ib.finki.ukim.totp.services.interfaces.transfer;

import java.time.LocalDateTime;

/**
 * A service interface for generating and verifying one-time passwords (TOTP).
 */
public interface IOneTimePasswordService {

      /**
       * Generates a Time-based One-Time Password (TOTP) using the provided user's key and current time.
       *
       * @param key The secret key used for TOTP generation.
       * @param currentTime The current time to base the TOTP on.
       * @return The generated TOTP as a string.
       */
      String generateTOTP(String key, LocalDateTime currentTime);

      /**
       * Verifies a Time-based One-Time Password (TOTP) against the provided secret key, password, and timestamp.
       *
       * @param secretKey The secret key used for TOTP verification.
       * @param password The TOTP password to verify.
       * @param timestamp The timestamp associated with the TOTP.
       * @return True if the TOTP is verified successfully, false otherwise.
       */
      boolean verifyTOTP(String secretKey, String password, LocalDateTime timestamp);
}
