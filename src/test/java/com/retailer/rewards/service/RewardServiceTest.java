package com.retailer.rewards.service;

import com.retailer.rewards.dto.CustomerRewardSummary;
import com.retailer.rewards.dto.MonthlyPoints;
import com.retailer.rewards.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RewardServiceTest {

    private RewardService rewardService;

    @BeforeEach
    void setUp() {
        // Uses the seeded demo data from TransactionRepository.
        rewardService = new RewardService(new TransactionRepository());
    }

    // ---- Single-transaction point calculation (the core business rule) ----

    @Test
    void spec_example_120_dollars_earns_90_points() {
        assertEquals(90, rewardService.calculatePoints(new BigDecimal("120.00")));
    }

    @Test
    void amount_at_or_below_50_earns_zero_points() {
        assertEquals(0, rewardService.calculatePoints(new BigDecimal("50.00")));
        assertEquals(0, rewardService.calculatePoints(new BigDecimal("30.00")));
        assertEquals(0, rewardService.calculatePoints(BigDecimal.ZERO));
    }

    @Test
    void amount_between_50_and_100_earns_one_point_per_dollar_over_50() {
        assertEquals(40, rewardService.calculatePoints(new BigDecimal("90.00")));
        assertEquals(1, rewardService.calculatePoints(new BigDecimal("51.00")));
    }

    @Test
    void amount_at_exactly_100_earns_the_full_50_point_band() {
        assertEquals(50, rewardService.calculatePoints(new BigDecimal("100.00")));
    }

    @Test
    void amount_over_100_earns_50_plus_two_points_per_dollar_over_100() {
        assertEquals(150, rewardService.calculatePoints(new BigDecimal("150.00")));
        assertEquals(850, rewardService.calculatePoints(new BigDecimal("500.00")));
    }

    @Test
    void fractional_cents_are_floored_not_rounded() {
        // (99.99 - 50) = 49.99 -> floors to 49
        assertEquals(49, rewardService.calculatePoints(new BigDecimal("99.99")));
        // (150.75 - 100) * 2 + 50 = 151.5 -> floors to 151
        assertEquals(151, rewardService.calculatePoints(new BigDecimal("150.75")));
    }

    @Test
    void null_or_negative_amount_earns_zero_points() {
        assertEquals(0, rewardService.calculatePoints(null));
        assertEquals(0, rewardService.calculatePoints(new BigDecimal("-10.00")));
    }

    // ---- Aggregation: per customer, per month, and total ----

    @Test
    void alice_monthly_and_total_points_match_expected_breakdown() {
        CustomerRewardSummary summary = rewardService.getCustomerSummary("A100");

        assertEquals("Alice Johnson", summary.getCustomerName());
        List<MonthlyPoints> months = summary.getMonthlyPoints();
        assertEquals(3, months.size());

        // Jan: 120 -> 90, 75.50 -> 25, 45 -> 0  => 115
        assertEquals("2024-01", months.get(0).getMonth());
        assertEquals(115, months.get(0).getPoints());

        // Feb: 200 -> 250, 60 -> 10 => 260
        assertEquals("2024-02", months.get(1).getMonth());
        assertEquals(260, months.get(1).getPoints());

        // Mar: 100 -> 50, 150.75 -> 151 => 201
        assertEquals("2024-03", months.get(2).getMonth());
        assertEquals(201, months.get(2).getPoints());

        assertEquals(115 + 260 + 201, summary.getTotalPoints());
    }

    @Test
    void bob_has_no_march_transactions_so_march_is_absent_not_zero() {
        CustomerRewardSummary summary = rewardService.getCustomerSummary("B200");

        List<MonthlyPoints> months = summary.getMonthlyPoints();
        assertEquals(2, months.size(), "Bob only has Jan and Feb activity");
        assertTrue(months.stream().noneMatch(m -> m.getMonth().equals("2024-03")));

        // Jan: 50 -> 0, 90 -> 40 => 40
        assertEquals(40, months.get(0).getPoints());
        // Feb: 110 -> 70, 99.99 -> 49 => 119
        assertEquals(119, months.get(1).getPoints());

        assertEquals(40 + 119, summary.getTotalPoints());
    }

    @Test
    void carol_never_crosses_the_threshold_so_every_month_is_zero() {
        CustomerRewardSummary summary = rewardService.getCustomerSummary("C300");

        assertEquals(0, summary.getTotalPoints());
        assertTrue(summary.getMonthlyPoints().stream().allMatch(m -> m.getPoints() == 0));
    }

    @Test
    void unknown_customer_returns_empty_summary_with_zero_points() {
        CustomerRewardSummary summary = rewardService.getCustomerSummary("NOBODY");

        assertEquals(0, summary.getTotalPoints());
        assertTrue(summary.getMonthlyPoints().isEmpty());
    }

    @Test
    void all_customer_summaries_are_sorted_by_customer_id() {
        List<CustomerRewardSummary> all = rewardService.getAllCustomerSummaries();

        assertEquals(4, all.size());
        assertEquals("A100", all.get(0).getCustomerId());
        assertEquals("B200", all.get(1).getCustomerId());
        assertEquals("C300", all.get(2).getCustomerId());
        assertEquals("D400", all.get(3).getCustomerId());
    }
}
