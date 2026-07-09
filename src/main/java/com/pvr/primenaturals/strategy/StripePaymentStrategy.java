package com.pvr.primenaturals.strategy;

import com.stripe.Stripe;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Refund;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.RefundCreateParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service("stripe")
public class StripePaymentStrategy implements PaymentStrategy {

    @Value("${stripe.api.key:}")
    private String apiKey;

    @PostConstruct
    public void init() {
        if (apiKey != null && !apiKey.isBlank() && !apiKey.equals("changeme")) {
            Stripe.apiKey = apiKey;
        }
    }

    @Override
    public Map<String, Object> createPaymentIntent(BigDecimal amount) throws Exception {
        if (apiKey == null || apiKey.isBlank() || apiKey.equals("changeme")) {
            // Mock response if credentials are not configured to facilitate interview testing
            Map<String, Object> mockResponse = new HashMap<>();
            mockResponse.put("id", "pi_mock_" + UUID.randomUUID().toString().substring(0, 8));
            mockResponse.put("amount", amount.multiply(new BigDecimal(100)).longValue());
            mockResponse.put("currency", "usd");
            mockResponse.put("status", "requires_payment_method");
            mockResponse.put("client_secret", "pi_mock_secret_" + UUID.randomUUID().toString());
            mockResponse.put("gateway", "Stripe (Mocked)");
            return mockResponse;
        }

        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(amount.multiply(new BigDecimal(100)).longValue())
                .setCurrency("usd")
                .build();

        PaymentIntent intent = PaymentIntent.create(params);

        Map<String, Object> response = new HashMap<>();
        response.put("id", intent.getId());
        response.put("amount", intent.getAmount());
        response.put("currency", intent.getCurrency());
        response.put("status", intent.getStatus());
        response.put("client_secret", intent.getClientSecret());
        response.put("gateway", "Stripe");

        return response;
    }

    @Override
    public Map<String, Object> processRefund(String transactionId, BigDecimal amount) throws Exception {
        if (apiKey == null || apiKey.isBlank() || apiKey.equals("changeme")) {
            Map<String, Object> mockResponse = new HashMap<>();
            mockResponse.put("id", "re_mock_" + UUID.randomUUID().toString().substring(0, 8));
            mockResponse.put("payment_intent", transactionId);
            mockResponse.put("amount", amount);
            mockResponse.put("status", "succeeded");
            mockResponse.put("gateway", "Stripe (Mocked)");
            return mockResponse;
        }

        RefundCreateParams params = RefundCreateParams.builder()
                .setPaymentIntent(transactionId)
                .setAmount(amount.multiply(new BigDecimal(100)).longValue())
                .build();

        Refund refund = Refund.create(params);

        Map<String, Object> response = new HashMap<>();
        response.put("id", refund.getId());
        response.put("payment_intent", refund.getPaymentIntent());
        response.put("amount", refund.getAmount());
        response.put("status", refund.getStatus());
        response.put("gateway", "Stripe");

        return response;
    }
}
