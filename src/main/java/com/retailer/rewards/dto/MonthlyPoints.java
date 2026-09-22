package com.retailer.rewards.dto;

/**
 * Points earned by a customer in a single calendar month.
 * month is formatted as "yyyy-MM" (e.g. "2024-01") for unambiguous sorting/display.
 */
public class MonthlyPoints {

    private String month;
    private int points;

    public MonthlyPoints() {
    }

    public MonthlyPoints(String month, int points) {
        this.month = month;
        this.points = points;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }
}
