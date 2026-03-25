package com.example.email_processing_service.ai.tools;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class ToolExecutionService {

    public Map<String, Object> orderService(String orderId) {
        Map<String, Object> data = new HashMap<>();
        data.put("order_id", orderId);
        data.put("status", "SHIPPED");
        data.put("eta", "2 business days");
        return data;
    }

    public Map<String, Object> userService(String email) {
        Map<String, Object> data = new HashMap<>();
        data.put("email", email);
        data.put("customer_tier", "GOLD");
        data.put("recent_tickets", 1);
        return data;
    }

    public Map<String, Object> knowledgeBaseSearch(String query) {
        Map<String, Object> data = new HashMap<>();
        data.put("query", query);
        data.put("answer", "Our standard refund window is 30 days from delivery.");
        return data;
    }
}
