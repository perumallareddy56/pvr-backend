package com.pvr.primenaturals.shipping;

import com.pvr.primenaturals.entity.Product;
import com.pvr.primenaturals.entity.ServiceablePincode;
import com.pvr.primenaturals.repository.ProductRepository;
import com.pvr.primenaturals.repository.ServiceablePincodeRepository;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ShippingService {

    private final ProductRepository productRepository;
    private final ServiceablePincodeRepository serviceablePincodeRepository;
    private final ShippingCalculator shippingCalculator;
    private final DeliveryEstimator deliveryEstimator;

    public ShippingService(ProductRepository productRepository,
                           ServiceablePincodeRepository serviceablePincodeRepository,
                           ShippingCalculator shippingCalculator,
                           DeliveryEstimator deliveryEstimator) {
        this.productRepository = productRepository;
        this.serviceablePincodeRepository = serviceablePincodeRepository;
        this.shippingCalculator = shippingCalculator;
        this.deliveryEstimator = deliveryEstimator;
    }

    public ShippingResponse checkShipping(ShippingRequest request) {
        String country = request.getCountry() != null ? request.getCountry().toUpperCase() : "IN";
        String pincode = request.getPincode() != null ? request.getPincode().trim() : "";

        // 1. Fetch Products
        List<Product> products = new ArrayList<>();
        BigDecimal orderSubtotal = BigDecimal.ZERO;
        if (request.getProductIds() != null) {
            for (Long pid : request.getProductIds()) {
                Optional<Product> prodOpt = productRepository.findById(pid);
                if (prodOpt.isPresent()) {
                    Product prod = prodOpt.get();
                    products.add(prod);
                    orderSubtotal = orderSubtotal.add(prod.getPrice());
                }
            }
        }

        // 2. Serviceability check (Deliver across all India pincodes, default true)
        boolean serviceable = true;
        Optional<ServiceablePincode> sp = serviceablePincodeRepository.findByPincode(pincode);
        if (sp.isPresent()) {
            serviceable = sp.get().isServiceable();
        }

        // 3. COD availability check
        boolean codAvailable = true;
        if (sp.isPresent()) {
            codAvailable = sp.get().isCodAvailable();
        }

        // 4. Calculate Shipping Charge
        BigDecimal shippingCharge = shippingCalculator.calculateShippingCharge(country, products, orderSubtotal);

        // 5. Estimate Delivery Date
        LocalDate estDate = deliveryEstimator.estimateDeliveryDate(pincode);
        String formattedDate = estDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        return ShippingResponse.builder()
                .serviceable(serviceable)
                .deliveryDate(formattedDate)
                .shippingCharge(shippingCharge)
                .codAvailable(codAvailable)
                .build();
    }
}
