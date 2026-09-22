# Retailer Rewards API

A Spring Boot REST API that calculates customer reward points from transaction
history, broken down by month and totaled over the reporting period.

## Reward rule

For a single transaction:
- **2 points** for every dollar spent **over $100**
- **1 point** for every dollar spent **between $50 and $100**
- **0 points** for the portion at or below $50

> Example: a $120 purchase = 2×$20 (amount over $100) + 1×$50 (amount from $50–100) = **90 points**

The fractional-dollar portion of each band is floored, so points are always a
whole number (e.g. a $99.99 purchase earns 49 points, not 49.99).

## Tech stack

- Java 17
- Spring Boot 3.3.4 (Spring Web, Spring Validation)
- Maven
- JUnit 5 + MockMvc for tests
- In-memory repository (no external database needed to run this) seeded with
  a three-month sample data set

## Project structure

```
src/main/java/com/retailer/rewards/
├── RewardsApiApplication.java     # Spring Boot entry point
├── model/Transaction.java         # Domain model
├── dto/                            # API request/response shapes
│   ├── TransactionRequest.java
│   ├── MonthlyPoints.java
│   └── CustomerRewardSummary.java
├── repository/TransactionRepository.java  # In-memory store + seed data
├── service/RewardService.java     # Core points calculation + aggregation
├── controller/
│   ├── RewardController.java      # /api/rewards
│   └── TransactionController.java # /api/transactions
└── exception/                     # 404 / validation error handling
```

## Running it

```bash
mvn spring-boot:run
```

The API starts on `http://localhost:8080`.

## Running the tests

```bash
mvn test
```

Tests cover the point-calculation rule at every band boundary (≤50, 50–100,
exactly 100, >100), fractional-cent flooring, per-customer/per-month
aggregation, a customer who skips a month entirely, a customer who never
crosses the reward threshold, and the REST endpoints end-to-end (including
validation and 404 handling).

## Sample data set

Seeded automatically on startup — four customers across Jan–Mar 2024,
deliberately covering every case:

| Customer | Notes |
|---|---|
| **A100 – Alice Johnson** | Mix of low/mid/high purchases in all three months |
| **B200 – Bob Martinez** | Moderate spender; **no transactions in March** (tests that missing months are simply absent, not zero) |
| **C300 – Carol Nguyen** | Every purchase is under $50 — **always earns 0 points** |
| **D400 – Dave Kim** | Single large purchase each month (300–500 range) |

## Endpoints

### `GET /api/rewards`
Reward summary (monthly breakdown + total) for every customer on record.

```bash
curl http://localhost:8080/api/rewards
```

```json
[
  {
    "customerId": "A100",
    "customerName": "Alice Johnson",
    "monthlyPoints": [
      { "month": "2024-01", "points": 115 },
      { "month": "2024-02", "points": 260 },
      { "month": "2024-03", "points": 201 }
    ],
    "totalPoints": 576
  },
  {
    "customerId": "B200",
    "customerName": "Bob Martinez",
    "monthlyPoints": [
      { "month": "2024-01", "points": 40 },
      { "month": "2024-02", "points": 119 }
    ],
    "totalPoints": 159
  },
  {
    "customerId": "C300",
    "customerName": "Carol Nguyen",
    "monthlyPoints": [
      { "month": "2024-01", "points": 0 },
      { "month": "2024-02", "points": 0 },
      { "month": "2024-03", "points": 0 }
    ],
    "totalPoints": 0
  },
  {
    "customerId": "D400",
    "customerName": "Dave Kim",
    "monthlyPoints": [
      { "month": "2024-01", "points": 850 },
      { "month": "2024-02", "points": 490 },
      { "month": "2024-03", "points": 670 }
    ],
    "totalPoints": 2010
  }
]
```

### `GET /api/rewards/{customerId}`
Reward summary for a single customer. Returns `404` if the customer has no
recorded transactions.

```bash
curl http://localhost:8080/api/rewards/A100
```

```json
{
  "customerId": "A100",
  "customerName": "Alice Johnson",
  "monthlyPoints": [
    { "month": "2024-01", "points": 115 },
    { "month": "2024-02", "points": 260 },
    { "month": "2024-03", "points": 201 }
  ],
  "totalPoints": 576
}
```

Unknown customer:

```bash
curl -i http://localhost:8080/api/rewards/UNKNOWN
```

```json
{
  "timestamp": "2026-09-22T23:16:08.700910Z",
  "status": 404,
  "error": "Not Found",
  "message": "No transactions found for customerId: UNKNOWN"
}
```

### `GET /api/transactions`
Lists every recorded transaction (the raw ledger behind the rewards figures).

```bash
curl http://localhost:8080/api/transactions
```

```json
[
  {
    "id": 1,
    "customerId": "A100",
    "customerName": "Alice Johnson",
    "amount": 120.00,
    "transactionDate": "2024-01-15"
  },
  {
    "id": 2,
    "customerId": "A100",
    "customerName": "Alice Johnson",
    "amount": 75.50,
    "transactionDate": "2024-01-22"
  },
  {
    "id": 3,
    "customerId": "A100",
    "customerName": "Alice Johnson",
    "amount": 45.00,
    "transactionDate": "2024-01-28"
  },
  {
    "id": 4,
    "customerId": "A100",
    "customerName": "Alice Johnson",
    "amount": 200.00,
    "transactionDate": "2024-02-03"
  },
  {
    "id": 5,
    "customerId": "A100",
    "customerName": "Alice Johnson",
    "amount": 60.00,
    "transactionDate": "2024-02-18"
  },
  {
    "id": 6,
    "customerId": "A100",
    "customerName": "Alice Johnson",
    "amount": 100.00,
    "transactionDate": "2024-03-05"
  },
  {
    "id": 7,
    "customerId": "A100",
    "customerName": "Alice Johnson",
    "amount": 150.75,
    "transactionDate": "2024-03-20"
  },
  {
    "id": 8,
    "customerId": "B200",
    "customerName": "Bob Martinez",
    "amount": 50.00,
    "transactionDate": "2024-01-10"
  },
  {
    "id": 9,
    "customerId": "B200",
    "customerName": "Bob Martinez",
    "amount": 90.00,
    "transactionDate": "2024-01-25"
  },
  {
    "id": 10,
    "customerId": "B200",
    "customerName": "Bob Martinez",
    "amount": 110.00,
    "transactionDate": "2024-02-08"
  },
  {
    "id": 11,
    "customerId": "B200",
    "customerName": "Bob Martinez",
    "amount": 99.99,
    "transactionDate": "2024-02-27"
  },
  {
    "id": 12,
    "customerId": "C300",
    "customerName": "Carol Nguyen",
    "amount": 20.00,
    "transactionDate": "2024-01-05"
  },
  {
    "id": 13,
    "customerId": "C300",
    "customerName": "Carol Nguyen",
    "amount": 35.50,
    "transactionDate": "2024-02-14"
  },
  {
    "id": 14,
    "customerId": "C300",
    "customerName": "Carol Nguyen",
    "amount": 49.99,
    "transactionDate": "2024-03-30"
  },
  {
    "id": 15,
    "customerId": "D400",
    "customerName": "Dave Kim",
    "amount": 500.00,
    "transactionDate": "2024-01-12"
  },
  {
    "id": 16,
    "customerId": "D400",
    "customerName": "Dave Kim",
    "amount": 320.25,
    "transactionDate": "2024-02-20"
  },
  {
    "id": 17,
    "customerId": "D400",
    "customerName": "Dave Kim",
    "amount": 410.00,
    "transactionDate": "2024-03-09"
  }
]
```

### `GET /api/transactions/{customerId}`
Lists a single customer's transactions.

### `POST /api/transactions`
Records a new purchase. Its points are immediately reflected in `/api/rewards`.

```bash
curl -X POST http://localhost:8080/api/transactions \
  -H "Content-Type: application/json" \
  -d '{
        "customerId": "E500",
        "customerName": "Erin Patel",
        "amount": 130.00,
        "transactionDate": "2024-02-14"
      }'
```

```json
{
  "id": 18,
  "customerId": "E500",
  "customerName": "Erin Patel",
  "amount": 130.00,
  "transactionDate": "2024-02-14"
}
```

Immediately reflected in `GET /api/rewards/E500`:

```bash
curl http://localhost:8080/api/rewards/E500
```

```json
{
  "customerId": "E500",
  "customerName": "Erin Patel",
  "monthlyPoints": [
    { "month": "2024-02", "points": 110 }
  ],
  "totalPoints": 110
}
```

## Design notes

- **BigDecimal** is used throughout for money math to avoid floating-point
  rounding errors.
- A month with **zero transactions is omitted** from `monthlyPoints` rather
  than shown as `0`, so the response distinguishes "no activity" from
  "activity that earned no points" (see Carol vs. Bob in the sample data).
- The in-memory repository is intentionally simple (no database) to keep the
  exercise self-contained; swapping in a Spring Data JPA repository later
  would only mean changing `TransactionRepository`, not the service or
  controller layers.
  