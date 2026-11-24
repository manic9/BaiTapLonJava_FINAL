package com.esports.manager.config;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

public final class PasswordHasher {

    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int SALT_BYTES = 16;
    private static final int HASH_BITS = 256;
    private static final int ITERATIONS = 65_536;

    public record HashedPassword(String hashBase64, String saltBase64) {}

    private PasswordHasher() {}

    public static HashedPassword hash(char[] plainPassword) {
        if (plainPassword == null || plainPassword.length == 0) {
            throw new IllegalArgumentException("Mật khẩu không được để trống.");
        }
        byte[] salt = new byte[SALT_BYTES];
        new SecureRandom().nextBytes(salt);
        byte[] hash = deriveKey(plainPassword, salt);
        Arrays.fill(plainPassword, '\0');

        return new HashedPassword(
                Base64.getEncoder().encodeToString(hash),
                Base64.getEncoder().encodeToString(salt)
        );
    }

    public static boolean verify(char[] plainPassword, String expectedHashBase64, String saltBase64) {
        if (plainPassword == null || expectedHashBase64 == null || saltBase64 == null) {
            return false;
        }
        try {
            byte[] salt = Base64.getDecoder().decode(saltBase64);
            byte[] expectedHash = Base64.getDecoder().decode(expectedHashBase64);
            byte[] actualHash = deriveKey(plainPassword, salt);
            return MessageDigest.isEqual(expectedHash, actualHash);
        } catch (IllegalArgumentException e) {
            return false;
        } finally {
            Arrays.fill(plainPassword, '\0');
        }
    }

    private static byte[] deriveKey(char[] password, byte[] salt) {
        PBEKeySpec spec = new PBEKeySpec(password, salt, ITERATIONS, HASH_BITS);
        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITHM);
            return factory.generateSecret(spec).getEncoded();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Không thể khởi tạo thuật toán băm PBKDF2", e);
        } finally {
            spec.clearPassword();
        }
    }
}