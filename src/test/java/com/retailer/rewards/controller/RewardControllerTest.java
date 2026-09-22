package com.retailer.rewards.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class RewardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getAllRewards_returnsAllSeededCustomers() throws Exception {
        mockMvc.perform(get("/api/rewards"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(4))
                .andExpect(jsonPath("$[0].customerId").value("A100"));
    }

    @Test
    void getRewardsForCustomer_returnsCorrectTotal() throws Exception {
        mockMvc.perform(get("/api/rewards/A100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerName").value("Alice Johnson"))
                .andExpect(jsonPath("$.totalPoints").value(576));
    }

    @Test
    void getRewardsForCustomer_unknownCustomer_returns404() throws Exception {
        mockMvc.perform(get("/api/rewards/UNKNOWN"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void createTransaction_thenReflectedInRewards() throws Exception {
        String newTransactionJson = """
                {
                  "customerId": "E500",
                  "customerName": "Erin Patel",
                  "amount": 130.00,
                  "transactionDate": "2024-02-14"
                }
                """;

        mockMvc.perform(post("/api/transactions")
                        .contentType("application/json")
                        .content(newTransactionJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.customerId").value("E500"));

        // 130 -> (130-100)*2 + 50 = 110 points
        mockMvc.perform(get("/api/rewards/E500"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPoints").value(110));
    }

    @Test
    void createTransaction_invalidPayload_returns400() throws Exception {
        String badJson = """
                {
                  "customerId": "",
                  "customerName": "No Amount",
                  "amount": -5,
                  "transactionDate": "2024-01-01"
                }
                """;

        mockMvc.perform(post("/api/transactions")
                        .contentType("application/json")
                        .content(badJson))
                .andExpect(status().isBadRequest());
    }
}
