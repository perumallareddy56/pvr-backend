package com.pvr.primenaturals.service;

import com.pvr.primenaturals.dto.response.OrderDTO;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.nio.charset.StandardCharsets;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private TemplateEngine templateEngine;

    @org.springframework.beans.factory.annotation.Value("${spring.mail.username}")
    private String mailFrom;

    @org.springframework.beans.factory.annotation.Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    public void sendOrderConfirmation(OrderDTO order, String recipientEmail) {
        try {
            Context context = new Context();
            context.setVariable("order", order);
            context.setVariable("userName", order.getUserName());
            context.setVariable("frontendUrl", frontendUrl);
            
            String process = templateEngine.process("email/order-confirmation", context);
            
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, StandardCharsets.UTF_8.name());

            helper.setFrom(mailFrom);
            helper.setTo(recipientEmail);
            helper.setSubject("PVR Order Confirmation: #" + order.getId());
            helper.setText(process, true);

            mailSender.send(mimeMessage);
        } catch (Exception e) {
            // Log the error but don't fail the order process
            System.err.println("CRITICAL: Failed to send order confirmation email: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void sendOrderStatusUpdate(OrderDTO order, String recipientEmail) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, false, StandardCharsets.UTF_8.name());

            helper.setFrom(mailFrom);
            helper.setTo(recipientEmail);
            helper.setSubject("Order Update: Your PVR Order #" + order.getId() + " is now " + order.getStatus());
            
            String text = "Namaste " + order.getUserName() + "!\n\n" +
                          "We are writing to inform you that your PVR Gourmet Order #" + order.getId() + " has been updated.\n" +
                          "New Status: **" + order.getStatus() + "**\n\n" +
                          "Total Amount: Rs " + order.getTotalAmount() + "\n" +
                          "Payment Method: " + order.getPaymentMethod() + "\n\n" +
                          "Thank you for choosing PVR!\nTrack your order anytime at our portal.";
                          
            helper.setText(text, false);
            mailSender.send(mimeMessage);
        } catch (Exception e) {
            System.err.println("CRITICAL: Failed to send order status update email: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void sendPasswordResetEmail(String recipientEmail, String resetLink) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, false, StandardCharsets.UTF_8.name());

            helper.setFrom(mailFrom);
            helper.setTo(recipientEmail);
            helper.setSubject("PVR Account Recovery: Reset Your Password");
            
            String text = "Namaste!\n\n" +
                          "We received a request to reset your PVR Prime Naturals password.\n" +
                          "Click the link below to set a new password. This link will expire in 1 hour.\n\n" +
                          resetLink + "\n\n" +
                          "If you did not request this, please ignore this email.\n\n" +
                          "Stay safe,\nThe PVR Security Team";
                          
            helper.setText(text, false);
            mailSender.send(mimeMessage);
        } catch (Exception e) {
            System.err.println("CRITICAL: Failed to send password reset email: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
