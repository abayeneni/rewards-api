package com.retailer.rewards.repository;

import com.retailer.rewards.model.Transaction;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Simple in-memory store of transactions, seeded on startup with a three-month
 * sample data set so the rewards endpoints have something meaningful to return
 * out of the box. In a real system this would be backed by a database.
 */
@Repository
public class TransactionRepository {

    private final List<Transaction> transactions = new ArrayList<>();
    private final AtomicLong idSequence = new AtomicLong(0);

    public TransactionRepository() {
        seedData();
    }

    public Transaction save(Transaction transaction) {
        transaction.setId(idSequence.incrementAndGet());
        transactions.add(transaction);
        return transaction;
    }

    public List<Transaction> findAll() {
        return Collections.unmodifiableList(transactions);
    }

    public List<Transaction> findByCustomerId(String customerId) {
        return transactions.stream()
                .filter(t -> t.getCustomerId().equalsIgnoreCase(customerId))
                .collect(Collectors.toList());
    }

    /**
     * Seeds a three-month (Jan-Mar 2024) transaction history for three customers,
     * deliberately covering every band of the rewards rule:
     *   - amounts at/below $50           -> 0 points
     *   - amounts strictly between 50-100 -> 1 point per dollar over 50
     *   - amounts over $100               -> 2 points per dollar over 100, plus the 50 base points
     *   - a customer with only low-value purchases (0 points every month)
     *   - a customer with no purchases in one of the three months
     */
    private void seedData() {
        // --- Alice Johnson (A100): mix of high, mid and low purchases across all 3 months ---
        add("A100", "Alice Johnson", "120.00", LocalDate.of(2024, 1, 15)); // 90 pts
        add("A100", "Alice Johnson", "75.50", LocalDate.of(2024, 1, 22));  // 25 pts
        add("A100", "Alice Johnson", "45.00", LocalDate.of(2024, 1, 28));  // 0 pts
        add("A100", "Alice Johnson", "200.00", LocalDate.of(2024, 2, 3));  // 250 pts
        add("A100", "Alice Johnson", "60.00", LocalDate.of(2024, 2, 18));  // 10 pts
        add("A100", "Alice Johnson", "100.00", LocalDate.of(2024, 3, 5));  // 50 pts
        add("A100", "Alice Johnson", "150.75", LocalDate.of(2024, 3, 20)); // 151 pts (150.75-100)*2+50=151.5->151

        // --- Bob Martinez (B200): moderate spender, skips March entirely ---
        add("B200", "Bob Martinez", "50.00", LocalDate.of(2024, 1, 10));   // 0 pts (exactly at threshold)
        add("B200", "Bob Martinez", "90.00", LocalDate.of(2024, 1, 25));   // 40 pts
        add("B200", "Bob Martinez", "110.00", LocalDate.of(2024, 2, 8));   // 70 pts
        add("B200", "Bob Martinez", "99.99", LocalDate.of(2024, 2, 27));   // 49 pts (floored)
        // no transactions in March for Bob

        // --- Carol Nguyen (C300): only ever spends below the reward threshold ---
        add("C300", "Carol Nguyen", "20.00", LocalDate.of(2024, 1, 5));    // 0 pts
        add("C300", "Carol Nguyen", "35.50", LocalDate.of(2024, 2, 14));   // 0 pts
        add("C300", "Carol Nguyen", "49.99", LocalDate.of(2024, 3, 30));   // 0 pts

        // --- Dave Kim (D400): big spender, single large transaction per month ---
        add("D400", "Dave Kim", "500.00", LocalDate.of(2024, 1, 12));      // 850 pts
        add("D400", "Dave Kim", "320.25", LocalDate.of(2024, 2, 20));      // 490 pts
        add("D400", "Dave Kim", "410.00", LocalDate.of(2024, 3, 9));       // 670 pts
    }

    private void add(String customerId, String customerName, String amount, LocalDate date) {
        save(new Transaction(null, customerId, customerName, new BigDecimal(amount), date));
    }
}
