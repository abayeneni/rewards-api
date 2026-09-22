package com.retailer.rewards.service;

import com.retailer.rewards.dto.CustomerRewardSummary;
import com.retailer.rewards.dto.MonthlyPoints;
import com.retailer.rewards.model.Transaction;
import com.retailer.rewards.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Contains the reward-points business rule and the aggregation logic that
 * rolls per-transaction points up into per-customer, per-month and total
 * figures.
 *
 * Rule: for a single transaction amount,
 *   - 2 points for every dollar spent over $100
 *   - 1 point for every dollar spent between $50 and $100
 *   - 0 points for the portion at or below $50
 *
 * e.g. $120 -> 2*20 (amount over 100) + 1*50 (amount between 50 and 100) = 90 points
 */
@Service
public class RewardService {

    private static final BigDecimal LOWER_THRESHOLD = BigDecimal.valueOf(50);
    private static final BigDecimal UPPER_THRESHOLD = BigDecimal.valueOf(100);
    private static final DateTimeFormatter MONTH_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM");

    private final TransactionRepository transactionRepository;

    public RewardService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    /**
     * Calculates the reward points earned for a single transaction amount.
     * The fractional-dollar portion of each band is floored, so points are
     * always a whole number and partial cents never round in the customer's favor.
     */
    public int calculatePoints(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return 0;
        }

        BigDecimal points = BigDecimal.ZERO;

        if (amount.compareTo(UPPER_THRESHOLD) > 0) {
            BigDecimal overUpper = amount.subtract(UPPER_THRESHOLD);
            points = points.add(overUpper.multiply(BigDecimal.valueOf(2)));
            points = points.add(BigDecimal.valueOf(50)); // full 1x credit for the 50-100 band
        } else if (amount.compareTo(LOWER_THRESHOLD) > 0) {
            BigDecimal overLower = amount.subtract(LOWER_THRESHOLD);
            points = points.add(overLower);
        }

        return points.setScale(0, RoundingMode.FLOOR).intValue();
    }

    /** Reward summaries for every customer who has at least one transaction on record. */
    public List<CustomerRewardSummary> getAllCustomerSummaries() {
        Map<String, List<Transaction>> byCustomer = transactionRepository.findAll().stream()
                .collect(Collectors.groupingBy(Transaction::getCustomerId));

        return byCustomer.keySet().stream()
                .map(this::buildSummary)
                .sorted(Comparator.comparing(CustomerRewardSummary::getCustomerId))
                .collect(Collectors.toList());
    }

    /** Reward summary for one customer, or an empty summary (all zeros) if they have no transactions. */
    public CustomerRewardSummary getCustomerSummary(String customerId) {
        return buildSummary(customerId);
    }

    private CustomerRewardSummary buildSummary(String customerId) {
        List<Transaction> customerTransactions = transactionRepository.findByCustomerId(customerId);

        String customerName = customerTransactions.stream()
                .findFirst()
                .map(Transaction::getCustomerName)
                .orElse(customerId);

        // TreeMap keeps months in chronological order in the response.
        Map<YearMonth, Integer> pointsByMonth = new TreeMap<>();
        int total = 0;

        for (Transaction t : customerTransactions) {
            int points = calculatePoints(t.getAmount());
            YearMonth month = YearMonth.from(t.getTransactionDate());
            pointsByMonth.merge(month, points, Integer::sum);
            total += points;
        }

        List<MonthlyPoints> monthlyPoints = new ArrayList<>();
        for (Map.Entry<YearMonth, Integer> entry : pointsByMonth.entrySet()) {
            monthlyPoints.add(new MonthlyPoints(entry.getKey().format(MONTH_FORMAT), entry.getValue()));
        }

        return new CustomerRewardSummary(customerId, customerName, monthlyPoints, total);
    }
}
