package com.pvr.primenaturals.strategy;

import java.math.BigDecimal;
import java.util.Map;

public interface PaymentStrategy {
    Map<String, Object> createPaymentIntent(BigDecimal amount) throws Exception;
    Map<String, Object> processRefund(String transactionId, BigDecimal amount) throws Exception;
}
