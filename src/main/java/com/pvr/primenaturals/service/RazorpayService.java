package com.pvr.primenaturals.service;
 
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
 
import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;
 
@Service
public class RazorpayService {
 
    @Value("${razorpay.key.id}")
    private String keyId;
 
    @Value("${razorpay.key.secret}")
    private String keySecret;
 
    private RazorpayClient client;
 
    @PostConstruct
    public void init() throws RazorpayException {
        System.out.println("DEBUG: Initializing Razorpay Client with Key ID: " + keyId);
        if (keyId != null && !keyId.isBlank() && !keyId.equals("changeme")) {
            this.client = new RazorpayClient(keyId, keySecret);
        } else {
            System.err.println("Razorpay credentials are not configured. RazorpayService will not be functional.");
        }
    }
 
    public Map<String, Object> createOrder(BigDecimal amount) throws RazorpayException {
        System.out.println("DEBUG: Creating LIVE Razorpay order for amount: " + amount);
        
        JSONObject orderRequest = new JSONObject();
        // Razorpay expects amount in paise (1 INR = 100 Paise)
        int paiseAmount = amount.multiply(new BigDecimal(100)).intValue();
        orderRequest.put("amount", paiseAmount);
        orderRequest.put("currency", "INR");
        String receipt = "txn_" + UUID.randomUUID().toString().substring(0, 8);
        orderRequest.put("receipt", receipt);
 
        if (this.client == null) {
            throw new RuntimeException("Razorpay client not initialized properly.");
        }
 
        Order order = client.orders.create(orderRequest);
        
        // Manual mapping for the response
        Map<String, Object> response = new java.util.HashMap<>();
        response.put("id", order.get("id").toString());
        response.put("entity", order.get("entity").toString());
        response.put("amount", order.get("amount"));
        response.put("amount_paid", order.get("amount_paid"));
        response.put("amount_due", order.get("amount_due"));
        response.put("currency", order.get("currency").toString());
        response.put("receipt", order.get("receipt").toString());
        response.put("status", order.get("status").toString());
        response.put("attempts", order.get("attempts"));
        response.put("created_at", order.get("created_at"));
        
        return response;
    }

    public boolean verifyPaymentSignature(String orderId, String paymentId, String signature) {
        try {
            JSONObject options = new JSONObject();
            options.put("razorpay_order_id", orderId);
            options.put("razorpay_payment_id", paymentId);
            options.put("razorpay_signature", signature);
            return com.razorpay.Utils.verifyPaymentSignature(options, keySecret);
        } catch (Exception e) {
            System.err.println("Razorpay signature verification failed: " + e.getMessage());
            return false;
        }
    }
}
