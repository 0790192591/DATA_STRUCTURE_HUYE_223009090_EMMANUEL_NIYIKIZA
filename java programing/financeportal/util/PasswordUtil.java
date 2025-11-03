package com.financeportal.util;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.SecureRandom;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import java.util.Objects;
import java.security.MessageDigest;

/**
 * Password utilities using PBKDF2WithHmacSHA256.
 *
 * Storage format (single string): iterations:saltBase64:hashBase64
 *
 * Example:
 *   100000:AbCd...base64...=:XyZ...base64...=
 *
 * Notes:
 *  - Use PasswordUtil.hashPassword(...) when storing a new password.
 *  - Use PasswordUtil.verifyPassword(plain, stored) to authenticate.
 */
public final class PasswordUtil {
    // Default parameters (can be tuned)
    public static final int DEFAULT_ITERATIONS = 100_000;
    public static final int SALT_LENGTH_BYTES = 16;      // 128-bit salt
    public static final int HASH_LENGTH_BYTES = 32;      // 256-bit output

    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private PasswordUtil() {}

    /**
     * Generate a salted PBKDF2 hash for the given plain password using default iterations.
     * Returns the encoded string suitable for storage.
     */
    public static String hashPassword(String password) {
        return hashPassword(password, DEFAULT_ITERATIONS);
    }

    /**
     * Generate a salted PBKDF2 hash for the given plain password using specified iterations.
     *
     * @param password plain password (must not be null)
     * @param iterations number of PBKDF2 iterations (>= 1)
     * @return encoded string "iterations:saltBase64:hashBase64"
     */
    public static String hashPassword(String password, int iterations) {
        Objects.requireNonNull(password, "password must not be null");
        if (iterations < 1) throw new IllegalArgumentException("iterations must be >= 1");

        byte[] salt = new byte[SALT_LENGTH_BYTES];
        SECURE_RANDOM.nextBytes(salt);

        byte[] hash = pbkdf2(password.toCharArray(), salt, iterations, HASH_LENGTH_BYTES);

        String encodedSalt = Base64.getEncoder().encodeToString(salt);
        String encodedHash = Base64.getEncoder().encodeToString(hash);

        return iterations + ":" + encodedSalt + ":" + encodedHash;
    }

    /**
     * Verify a plain password against a stored encoded password (format: iterations:salt:hash).
     *
     * @param passwordPlain plain password input from user
     * @param storedEncoded stored representation from DB
     * @return true if password matches
     */
    public static boolean verifyPassword(String passwordPlain, String storedEncoded) {
        if (passwordPlain == null || storedEncoded == null) return false;

        String[] parts = storedEncoded.split(":");
        if (parts.length != 3) return false;

        int iterations;
        try {
            iterations = Integer.parseInt(parts[0]);
        } catch (NumberFormatException ex) {
            return false;
        }

        byte[] salt;
        byte[] storedHash;
        try {
            salt = Base64.getDecoder().decode(parts[1]);
            storedHash = Base64.getDecoder().decode(parts[2]);
        } catch (IllegalArgumentException ex) {
            return false;
        }

        byte[] computedHash = pbkdf2(passwordPlain.toCharArray(), salt, iterations, storedHash.length);

        // Constant-time comparison to avoid timing attacks
        return MessageDigest.isEqual(storedHash, computedHash);
    }

    // PBKDF2 compute
    private static byte[] pbkdf2(char[] password, byte[] salt, int iterations, int bytes) {
        try {
            PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, bytes * 8);
            SecretKeyFactory skf = SecretKeyFactory.getInstance(ALGORITHM);
            byte[] res = skf.generateSecret(spec).getEncoded();
            spec.clearPassword();
            return res;
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException("Failed to compute PBKDF2 hash: " + e.getMessage(), e);
        }
    }
}
