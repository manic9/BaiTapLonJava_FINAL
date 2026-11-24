package com.esports.manager.config;

import java.io.Console;
import java.util.Arrays;
import java.util.Scanner;

/** Run from IntelliJ or a terminal to generate values for dbo.Users. */
public final class PasswordHashTool {
    private PasswordHashTool() {}

    public static void main(String[] args) {
        String username;
        char[] password;

        Console console = System.console();
        if (args.length >= 2) {
            username = args[0].trim();
            password = args[1].toCharArray();
        } else if (console != null) {
            username = console.readLine("Username: ").trim();
            password = console.readPassword("New password: ");
        } else {
            Scanner scanner = new Scanner(System.in);
            System.out.print("Username: ");
            username = scanner.hasNextLine() ? scanner.nextLine().trim() : "admin";
            System.out.print("New password: ");
            password = scanner.hasNextLine() ? scanner.nextLine().toCharArray() : new char[0];
        }

        if (username.isBlank() || password.length == 0) {
            System.err.println("Username và password không được để trống!");
            return;
        }

        try {
            PasswordHasher.HashedPassword result = PasswordHasher.hash(password);
            System.out.println("--------------------------------------------------");
            System.out.println("PasswordHash: " + result.hashBase64());
            System.out.println("PasswordSalt: " + result.saltBase64());
            System.out.println("--------------------------------------------------");
            System.out.println("Câu lệnh SQL để thêm vào dbo.Users:");
            System.out.printf(
                "INSERT INTO dbo.Users (UserName, PasswordHash, PasswordSalt, IsActive)%n" +
                "VALUES (N'%s', N'%s', N'%s', 1);%n",
                username.replace("'", "''"), result.hashBase64(), result.saltBase64()
            );
            System.out.println("--------------------------------------------------");
        } finally {
            Arrays.fill(password, '\0');
        }
    }
}

