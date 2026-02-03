package com.example.fitlife.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Utility class for password hashing using SHA-256
 */
public class PasswordHasher {
    /**
     * Hashes a password using SHA-256 algorithm
     */
    public static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return password; // Fallback to plain text if hashing fails
        }
    }
}
