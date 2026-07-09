package com.pvr.primenaturals.factory;

import com.pvr.primenaturals.strategy.PaymentStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class PaymentFactory {

    @Autowired
    private Map<String, PaymentStrategy> paymentStrategies;

    public PaymentStrategy getPaymentStrategy(String gatewayName) {
        PaymentStrategy strategy = paymentStrategies.get(gatewayName.toLowerCase());
        if (strategy == null) {
            throw new IllegalArgumentException("Unsupported payment gateway: " + gatewayName);
        }
        return strategy;
    }
}
