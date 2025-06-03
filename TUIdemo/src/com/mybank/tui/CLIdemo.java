package com.mybank.tui;

import com.mybank.domain.*;
import org.fusesource.jansi.AnsiConsole;
import org.jline.reader.*;
import org.jline.reader.impl.completer.*;
import org.jline.utils.*;

import java.io.PrintWriter;
import java.util.*;

public class CLIdemo {

    private static final String CLR_RESET = "\u001B[0m";
    private static final String CLR_RED = "\u001B[31m";
    private static final String CLR_GREEN = "\u001B[32m";
    private static final String CLR_YELLOW = "\u001B[33m";

    private final String[] availableCommands = {"help", "customers", "customer", "report", "exit"};

    public static void main(String[] args) {
        Bank.addCustomer("John", "Doe");
        Bank.addCustomer("Fox", "Mulder");

        Bank.getCustomer(0).addAccount(new CheckingAccount(2000));
        Bank.getCustomer(1).addAccount(new SavingsAccount(1000, 3));

        CLIdemo app = new CLIdemo();
        app.initialize();
        app.launch();
    }

    public void initialize() {
        AnsiConsole.systemInstall();
        showIntro();
    }

    public void launch() {
        LineReader reader = LineReaderBuilder.builder()
                .completer(new ArgumentCompleter(new StringsCompleter(availableCommands)))
                .build();

        String commandInput;

        while ((commandInput = readUserInput(reader)) != null) {
            if (commandInput.equals("help")) {
                displayHelp();
            } else if (commandInput.equals("customers")) {
                showAllCustomers();
            } else if (commandInput.startsWith("customer")) {
                showCustomerByIndex(commandInput);
            } else if (commandInput.equals("report")) {
                generateReport();
            } else if (commandInput.equals("exit")) {
                System.out.println("Terminating application.");
                break;
            } else {
                System.out.println(CLR_RED + "Unknown command. Type 'help' or press TAB." + CLR_RESET);
            }
        }

        AnsiConsole.systemUninstall();
    }

    private void showIntro() {
        System.out.println("\nWelcome to" + CLR_GREEN + " MyBank CLI" + CLR_RESET);
        System.out.println("Type 'help' or press TAB to see available commands.");
    }

    private String readUserInput(LineReader reader) {
        try {
            return reader.readLine(CLR_YELLOW + "\nbank> " + CLR_RESET).trim();
        } catch (UserInterruptException | EndOfFileException e) {
            return null;
        }
    }

    private void displayHelp() {
        System.out.println("help                 - Show list of commands");
        System.out.println("customers            - Display all customers");
        System.out.println("customer <number>    - Show customer details by index");
        System.out.println("report               - Show summary report");
        System.out.println("exit                 - Exit application");
    }

    private void showAllCustomers() {
        int count = Bank.getNumberOfCustomers();
        if (count == 0) {
            System.out.println(CLR_RED + "No customers found in the bank." + CLR_RESET);
            return;
        }

        System.out.println("\nCustomer List:");
        System.out.println("--------------------------------------------------");
        System.out.printf("%-15s %-15s %-10s\n", "Last Name", "First Name", "Balance");

        for (int i = 0; i < count; i++) {
            Customer client = Bank.getCustomer(i);
            double balance = client.getAccount(0).getBalance();
            System.out.printf("%-15s %-15s $%.2f\n", client.getLastName(), client.getFirstName(), balance);
        }
    }

    private void showCustomerByIndex(String input) {
        try {
            String[] tokens = input.split(" ");
            if (tokens.length < 2) throw new NumberFormatException();
            int index = Integer.parseInt(tokens[1]);

            Customer c = Bank.getCustomer(index);
            String accountKind = (c.getAccount(0) instanceof CheckingAccount) ? "Checking" : "Savings";

            System.out.println("\nCustomer Info:");
            System.out.println("--------------------------------------------------");
            System.out.printf("Name         : %s %s\n", c.getFirstName(), c.getLastName());
            System.out.printf("Account Type : %s\n", accountKind);
            System.out.printf("Balance      : $%.2f\n", c.getAccount(0).getBalance());

        } catch (Exception ex) {
            System.out.println(CLR_RED + "Invalid customer number!" + CLR_RESET);
        }
    }

    private void generateReport() {
        int totalClients = Bank.getNumberOfCustomers();

        if (totalClients == 0) {
            System.out.println(CLR_RED + "There are no customers to report." + CLR_RESET);
            return;
        }

        System.out.println("\nCustomer Report");
        System.out.println("===============================================================================");
        System.out.printf("%-15s %-15s %-15s %-15s %-15s\n",
                "Last Name", "First Name", "Checking", "Savings", "Total");

        double sumChecking = 0;
        double sumSavings = 0;

        for (int i = 0; i < totalClients; i++) {
            Customer c = Bank.getCustomer(i);
            double checking = 0, saving = 0;

            for (int j = 0; j < c.getNumberOfAccounts(); j++) {
                if (c.getAccount(j) instanceof CheckingAccount) {
                    checking += c.getAccount(j).getBalance();
                } else if (c.getAccount(j) instanceof SavingsAccount) {
                    saving += c.getAccount(j).getBalance();
                }
            }

            double total = checking + saving;

            System.out.printf("%-15s %-15s $%-14.2f $%-14.2f $%-14.2f\n",
                    c.getLastName(), c.getFirstName(), checking, saving, total);

            sumChecking += checking;
            sumSavings += saving;
        }

        double grandTotal = sumChecking + sumSavings;

        System.out.println("===============================================================================");
        System.out.printf("%-47s $%-14.2f $%-14.2f $%-14.2f\n",
                "TOTALS:", sumChecking, sumSavings, grandTotal);
    }
}
