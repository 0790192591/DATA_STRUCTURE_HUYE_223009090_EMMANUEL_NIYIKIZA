package com.financeportal.util;

import java.math.BigDecimal;
import java.util.regex.Pattern;

/**
 * Common validation utilities used across services and UI.
 *
 * - Simple, clear boolean checks for common fields
 * - Also provides validateOrThrow* methods for service-level usage
 */
public final class ValidationUtil {

    // Username: letters, digits, underscore, dot; between 3 and 30 chars
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[A-Za-z0-9_.]{3,30}$");

    // Email pattern: reasonably permissive but excludes obviously invalid strings
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    // Password strength: at least one lower, one upper, one digit, one special char, min length 8
    private static final int DEFAULT_MIN_PASSWORD_LENGTH = 8;
    private static final Pattern UPPERCASE = Pattern.compile(".*[A-Z].*");
    private static final Pattern LOWERCASE = Pattern.compile(".*[a-z].*");
    private static final Pattern DIGIT = Pattern.compile(".*\\d.*");
    private static final Pattern SPECIAL = Pattern.compile(".*[!@#$%^&*()_+\\-=[\\]{};':\"\\\\|,.<>/?].*");

    private ValidationUtil() {}

    /* -------------------- boolean checks -------------------- */

    public static boolean isNotBlank(String s) {
        return s != null && !s.trim().isEmpty();
    }

    public static boolean isValidUsername(String username) {
        return username != null && USERNAME_PATTERN.matcher(username).matches();
    }

    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    public static boolean isStrongPassword(String password) {
        if (password == null) return false;
        if (password.length() < DEFAULT_MIN_PASSWORD_LENGTH) return false;
        if (!UPPERCASE.matcher(password).find()) return false;
        if (!LOWERCASE.matcher(password).find()) return false;
        if (!DIGIT.matcher(password).find()) return false;
        if (!SPECIAL.matcher(password).find()) return false;
        return true;
    }

    public static boolean isPositive(BigDecimal value) {
        return value != null && value.compareTo(BigDecimal.ZERO) > 0;
    }

    public static boolean isNonNegative(BigDecimal value) {
        return value != null && value.compareTo(BigDecimal.ZERO) >= 0;
    }

    /* -------------------- validators that throw -------------------- */

    public static void validateUsernameOrThrow(String username) {
        if (!isValidUsername(username)) {
            throw new IllegalArgumentException("Username invalid. Allowed: letters, digits, underscore, dot, length 3-30.");
        }
    }

    public static void validateEmailOrThrow(String email) {
        if (!isValidEmail(email)) {
            throw new IllegalArgumentException("Email is invalid.");
        }
    }

    public static void validatePasswordOrThrow(String password) {
        if (!isStrongPassword(password)) {
            throw new IllegalArgumentException(
                    "Password must be at least " + DEFAULT_MIN_PASSWORD_LENGTH +
                    " characters and include upper-case, lower-case, digits and special character."
            );
        }
    }

    public static void validatePositiveOrThrow(BigDecimal amount, String fieldName) {
        if (!isPositive(amount)) {
            throw new IllegalArgumentException(fieldName + " must be positive.");
        }
    }
}
