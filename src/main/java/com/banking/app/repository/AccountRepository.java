package com.banking.app.repository;

import com.banking.app.model.Account;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Repository for managing accounts in memory.
 * This is a simple in-memory implementation for demonstration purposes.
 */
@Repository
public class AccountRepository {

    // In-memory storage for accounts
    private final Map<String, Account> accounts = new HashMap<>();

    /**
     * Constructor - Initialize with some dummy accounts
     */
    public AccountRepository() { // now have usernames and passwords
        // Create some default accounts for testing
        accounts.put("123", new Account("123", "John Doe", "johndoe", "123456", 12450.00, "VERIFIED"));
        accounts.put("456", new Account("456", "Jane Smith", "janesmith", "123456", 5000.00, "VERIFIED"));
        accounts.put("789", new Account("789", "Alice Johnson", "alicejohnson", "123456", 1000.00, "UNVERIFIED"));
        accounts.put("999", new Account("999", "Bob Brown", "bobbrown", "123456", 0.00, "SUSPENDED"));
    }

    /**
     * Find an account by account number
     * 
     * @param accountNumber The account number to search for
     * @return Optional containing the account if found, empty otherwise
     */
    public Optional<Account> findByAccountNumber(String accountNumber) {
        return Optional.ofNullable(accounts.get(accountNumber));
    }

    /**
     * Find an account by username
     * 
     * @param username The username to search for
     * @return Optional containing the account if found, empty otherwise
     */
    public Optional<Account> findAccountbyUsername(String username) {
        return accounts.values().stream()
                .filter(acc -> acc.getUsername().equalsIgnoreCase(username)) // Case-insensitive check
                .findFirst();
    }

    /**
     * Save or update an account
     * 
     * @param account The account to save
     * @return The saved account
     */
    public Account save(Account account) {
        accounts.put(account.getAccountNumber(), account);
        return account;
    }

    /**
     * Delete an account by account number
     * 
     * @param accountNumber The account number to delete
     * @return true if account was deleted, false if not found
     */
    public boolean deleteByAccountNumber(String accountNumber) {
        return accounts.remove(accountNumber) != null;
    }

    /**
     * Check if an account exists
     * 
     * @param accountNumber The account number to check
     * @return true if account exists, false otherwise
     */
    public boolean existsByAccountNumber(String accountNumber) {
        return accounts.containsKey(accountNumber);
    }

    /**
     * Get all accounts
     * 
     * @return Map of all accounts
     */
    public Map<String, Account> findAll() {
        return new HashMap<>(accounts);
    }

    /**
     * Get the count of all accounts
     * 
     * @return Number of accounts
     */
    public int count() {
        return accounts.size();
    }

    /**
     * Clear all accounts (useful for testing)
     */
    public void deleteAll() {
        accounts.clear();
    }
}
