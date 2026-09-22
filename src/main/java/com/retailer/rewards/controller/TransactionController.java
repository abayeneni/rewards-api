package com.retailer.rewards.controller;

import com.retailer.rewards.dto.TransactionRequest;
import com.retailer.rewards.model.Transaction;
import com.retailer.rewards.repository.TransactionRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Exposes the raw transaction ledger: lets you see the demo data set and
 * record new purchases so their reward points flow straight into /api/rewards.
 */
@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionRepository transactionRepository;

    public TransactionController(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    /** GET /api/transactions - all recorded transactions. */
    @GetMapping
    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }

    /** GET /api/transactions/{customerId} - all recorded transactions for one customer. */
    @GetMapping("/{customerId}")
    public List<Transaction> getTransactionsForCustomer(@PathVariable String customerId) {
        return transactionRepository.findByCustomerId(customerId);
    }

    /** POST /api/transactions - record a new purchase. */
    @PostMapping
    public ResponseEntity<Transaction> createTransaction(@Valid @RequestBody TransactionRequest request) {
        Transaction transaction = new Transaction(
                null,
                request.getCustomerId(),
                request.getCustomerName(),
                request.getAmount(),
                request.getTransactionDate()
        );
        Transaction saved = transactionRepository.save(transaction);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }
}
