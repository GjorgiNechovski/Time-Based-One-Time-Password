package ib.finki.ukim.totp.services.interfaces.authentication;

/**
 * A service interface responsible for salting and hashing operations for password security.
 */
public interface ISaltingService {

    /**
     * Validates whether a given input matches the hashed value considering the provided salt.
     *
     * @param input The input string to validate.
     * @param hashedValue The hashed value to compare against.
     * @param salt The salt used in the hashing process.
     * @return True if the input matches the hashed value, false otherwise.
     */
    boolean validate(String input, String hashedValue, byte[] salt);

    /**
     * Generates a random salt for password hashing.
     *
     * @return The generated salt as a byte array.
     */
    byte[] generateSalt();

    /**
     * Hashes the input string using the provided salt.
     *
     * @param input The input string to hash.
     * @param salt The salt used in the hashing process.
     * @return The hashed value as a string.
     */
    String hash(String input, byte[] salt);
}
