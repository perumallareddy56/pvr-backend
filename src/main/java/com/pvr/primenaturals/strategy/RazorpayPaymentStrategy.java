package com.pvr.primenaturals.strategy;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.Refund;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service("razorpay")
public class RazorpayPaymentStrategy implements PaymentStrategy {

    @Value("${razorpay.key.id}")
    private String keyId;

    @Value("${razorpay.key.secret}")
    private String keySecret;

    private RazorpayClient client;

    @PostConstruct
    public void init() throws Exception {
        if (keyId != null && !keyId.isBlank() && !keyId.equals("changeme")) {
            this.client = new RazorpayClient(keyId, keySecret);
        }
    }

    @Override
    public Map<String, Object> createPaymentIntent(BigDecimal amount) throws Exception {
        if (this.client == null) {
            // Mock response if credentials are not configured to facilitate interview testing
            Map<String, Object> mockResponse = new HashMap<>();
            mockResponse.put("id", "order_mock_" + UUID.randomUUID().toString().substring(0, 8));
            mockResponse.put("amount", amount.multiply(new BigDecimal(100)).intValue());
            mockResponse.put("currency", "INR");
            mockResponse.put("status", "created");
            mockResponse.put("gateway", "Razorpay (Mocked)");
            return mockResponse;
        }

        JSONObject orderRequest = new JSONObject();
        int paiseAmount = amount.multiply(new BigDecimal(100)).intValue();
        orderRequest.put("amount", paiseAmount);
        orderRequest.put("currency", "INR");
        String receipt = "txn_" + UUID.randomUUID().toString().substring(0, 8);
        orderRequest.put("receipt", receipt);

        Order order = client.orders.create(orderRequest);

        Map<String, Object> response = new HashMap<>();
        response.put("id", order.get("id").toString());
        response.put("amount", order.get("amount"));
        response.put("currency", order.get("currency").toString());
        response.put("receipt", order.get("receipt").toString());
        response.put("status", order.get("status").toString());
        response.put("gateway", "Razorpay");

        return response;
    }

    @Override
    public Map<String, Object> processRefund(String transactionId, BigDecimal amount) throws Exception {
        if (this.client == null) {
            Map<String, Object> mockResponse = new HashMap<>();
            mockResponse.put("id", "rfnd_mock_" + UUID.randomUUID().toString().substring(0, 8));
            mockResponse.put("payment_id", transactionId);
            mockResponse.put("amount", amount);
            mockResponse.put("status", "processed");
            mockResponse.put("gateway", "Razorpay (Mocked)");
            return mockResponse;
        }

        JSONObject refundRequest = new JSONObject();
        refundRequest.put("payment_id", transactionId);
        int paiseAmount = amount.multiply(new BigDecimal(100)).intValue();
        refundRequest.put("amount", paiseAmount);

        Refund refund = client.refunds.create(refundRequest);

        Map<String, Object> response = new HashMap<>();
        response.put("id", refund.get("id").toString());
        response.put("payment_id", refund.get("payment_id").toString());
        response.put("amount", refund.get("amount"));
        response.put("status", refund.get("status").toString());
        response.put("gateway", "Razorpay");

        return response;
    }
}
