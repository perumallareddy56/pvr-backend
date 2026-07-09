package com.pvr.primenaturals.config;

import com.pvr.primenaturals.entity.ServiceablePincode;
import com.pvr.primenaturals.repository.ServiceablePincodeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class PincodeDataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(PincodeDataInitializer.class);

    private final ServiceablePincodeRepository serviceablePincodeRepository;

    public PincodeDataInitializer(ServiceablePincodeRepository serviceablePincodeRepository) {
        this.serviceablePincodeRepository = serviceablePincodeRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("Seeding / Syncing serviceable pincodes...");

        List<ServiceablePincode> initialPincodes = List.of(
                ServiceablePincode.builder().pincode("560001").serviceable(true).estimatedDays(2).deliveryCharge(new BigDecimal("40.00")).codAvailable(true).build(),
                ServiceablePincode.builder().pincode("110001").serviceable(true).estimatedDays(3).deliveryCharge(new BigDecimal("50.00")).codAvailable(true).build(),
                ServiceablePincode.builder().pincode("400001").serviceable(true).estimatedDays(3).deliveryCharge(new BigDecimal("50.00")).codAvailable(true).build(),
                ServiceablePincode.builder().pincode("600001").serviceable(true).estimatedDays(2).deliveryCharge(new BigDecimal("40.00")).codAvailable(true).build(),
                ServiceablePincode.builder().pincode("500001").serviceable(true).estimatedDays(2).deliveryCharge(new BigDecimal("0.00")).codAvailable(true).build(), // free delivery
                ServiceablePincode.builder().pincode("700001").serviceable(true).estimatedDays(4).deliveryCharge(new BigDecimal("60.00")).codAvailable(false).build() // no COD
        );

        for (ServiceablePincode pin : initialPincodes) {
            serviceablePincodeRepository.findByPincode(pin.getPincode())
                .ifPresentOrElse(
                    existing -> {
                        existing.setCodAvailable(pin.isCodAvailable());
                        existing.setEstimatedDays(pin.getEstimatedDays());
                        existing.setDeliveryCharge(pin.getDeliveryCharge());
                        existing.setServiceable(pin.isServiceable());
                        serviceablePincodeRepository.save(existing);
                    },
                    () -> serviceablePincodeRepository.save(pin)
                );
        }
        log.info("Successfully synced {} serviceable pincodes.", initialPincodes.size());
    }
}
