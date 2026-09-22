package com.retailer.rewards.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Reward points earned by a customer, broken down by month, plus the total
 * across the whole reporting period.
 */
public class CustomerRewardSummary {

    private String customerId;
    private String customerName;
    private List<MonthlyPoints> monthlyPoints = new ArrayList<>();
    private int totalPoints;

    public CustomerRewardSummary() {
    }

    public CustomerRewardSummary(String customerId, String customerName, List<MonthlyPoints> monthlyPoints, int totalPoints) {
        this.customerId = customerId;
        this.customerName = customerName;
        this.monthlyPoints = monthlyPoints;
        this.totalPoints = totalPoints;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public List<MonthlyPoints> getMonthlyPoints() {
        return monthlyPoints;
    }

    public void setMonthlyPoints(List<MonthlyPoints> monthlyPoints) {
        this.monthlyPoints = monthlyPoints;
    }

    public int getTotalPoints() {
        return totalPoints;
    }

    public void setTotalPoints(int totalPoints) {
        this.totalPoints = totalPoints;
    }
}
