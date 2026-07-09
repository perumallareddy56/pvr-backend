package com.pvr.primenaturals.shipping;

import com.pvr.primenaturals.entity.ServiceablePincode;
import com.pvr.primenaturals.repository.ServiceablePincodeRepository;
import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.util.Optional;

@Component
public class DeliveryEstimator {

    private final ServiceablePincodeRepository serviceablePincodeRepository;

    public DeliveryEstimator(ServiceablePincodeRepository serviceablePincodeRepository) {
        this.serviceablePincodeRepository = serviceablePincodeRepository;
    }

    public LocalDate estimateDeliveryDate(String pincode) {
        // Warehouse processing: 6 hours
        // Packing: 3 hours
        // Courier pickup: 12 hours
        // Out for delivery: 6 hours
        // Total internal processing: 27 hours (~1.1 days)
        int internalHours = 6 + 3 + 12 + 6;

        // Transit days
        int transitDays = 3; // default fallback
        Optional<ServiceablePincode> sp = serviceablePincodeRepository.findByPincode(pincode);
        if (sp.isPresent() && sp.get().isServiceable()) {
            transitDays = sp.get().getEstimatedDays();
        }

        // Total delay in hours
        int totalHours = internalHours + (transitDays * 24);

        // Convert to days (round up)
        int totalDays = (int) Math.ceil(totalHours / 24.0);

        return LocalDate.now().plusDays(totalDays);
    }
}
