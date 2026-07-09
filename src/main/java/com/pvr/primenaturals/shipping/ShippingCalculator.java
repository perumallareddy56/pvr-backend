package com.pvr.primenaturals.shipping;

import com.pvr.primenaturals.entity.Product;
import com.pvr.primenaturals.entity.ShippingRule;
import com.pvr.primenaturals.repository.ShippingRuleRepository;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.util.List;

@Component
public class ShippingCalculator {

    private final ShippingRuleRepository shippingRuleRepository;

    public ShippingCalculator(ShippingRuleRepository shippingRuleRepository) {
        this.shippingRuleRepository = shippingRuleRepository;
    }

    public double parseWeight(String weightStr) {
        if (weightStr == null || weightStr.trim().isEmpty()) {
            return 0.25; // default to 250g
        }
        try {
            String clean = weightStr.toLowerCase().replaceAll("[^0-9.]", "").trim();
            double value = Double.parseDouble(clean);
            if (weightStr.toLowerCase().contains("kg")) {
                return value;
            } else if (weightStr.toLowerCase().contains("g")) {
                return value / 1000.0;
            }
            return value;
        } catch (Exception e) {
            return 0.25;
        }
    }

    public BigDecimal calculateShippingCharge(String country, List<Product> products, BigDecimal orderSubtotal) {
        double totalWeight = 0.0;
        for (Product product : products) {
            totalWeight += parseWeight(product.getWeight());
        }

        List<ShippingRule> activeRules = shippingRuleRepository.findByCountryAndActiveTrue(country);
        if (activeRules.isEmpty()) {
            // Fallback default
            return orderSubtotal.compareTo(new BigDecimal("499.00")) >= 0
                    ? BigDecimal.ZERO
                    : new BigDecimal("60.00");
        }

        // Find the matching rule
        ShippingRule matchedRule = null;
        for (ShippingRule rule : activeRules) {
            if (orderSubtotal.compareTo(rule.getMinOrder()) >= 0 && totalWeight <= rule.getMaxWeight()) {
                matchedRule = rule;
                break;
            }
        }

        if (matchedRule == null) {
            // Default to the first active rule
            matchedRule = activeRules.get(0);
        }

        if (orderSubtotal.compareTo(matchedRule.getFreeShippingAbove()) >= 0) {
            return BigDecimal.ZERO;
        }

        return matchedRule.getShippingCharge();
    }
}
