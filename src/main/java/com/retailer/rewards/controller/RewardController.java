package com.retailer.rewards.controller;

import com.retailer.rewards.dto.CustomerRewardSummary;
import com.retailer.rewards.exception.CustomerNotFoundException;
import com.retailer.rewards.repository.TransactionRepository;
import com.retailer.rewards.service.RewardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Exposes reward-point summaries: per customer, per month, and the running total.
 */
@RestController
@RequestMapping("/api/rewards")
public class RewardController {

    private final RewardService rewardService;
    private final TransactionRepository transactionRepository;

    public RewardController(RewardService rewardService, TransactionRepository transactionRepository) {
        this.rewardService = rewardService;
        this.transactionRepository = transactionRepository;
    }

    /** GET /api/rewards - reward summary (monthly + total) for every customer on record. */
    @GetMapping
    public List<CustomerRewardSummary> getAllRewards() {
        return rewardService.getAllCustomerSummaries();
    }

    /** GET /api/rewards/{customerId} - reward summary (monthly + total) for one customer. */
    @GetMapping("/{customerId}")
    public CustomerRewardSummary getRewardsForCustomer(@PathVariable String customerId) {
        if (transactionRepository.findByCustomerId(customerId).isEmpty()) {
            throw new CustomerNotFoundException(customerId);
        }
        return rewardService.getCustomerSummary(customerId);
    }
}
