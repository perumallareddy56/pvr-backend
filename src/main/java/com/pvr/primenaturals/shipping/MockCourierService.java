package com.pvr.primenaturals.shipping;

import com.pvr.primenaturals.entity.Courier;
import com.pvr.primenaturals.entity.Order;
import com.pvr.primenaturals.repository.CourierRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Random;

@Service
public class MockCourierService implements CourierService {

    private final CourierRepository courierRepository;
    private final Random random = new Random();

    public MockCourierService(CourierRepository courierRepository) {
        this.courierRepository = courierRepository;
    }

    @Override
    public List<Courier> getActiveCouriers() {
        return courierRepository.findByActiveTrue();
    }

    @Override
    public String assignCourierAndTracking(Order order) {
        List<Courier> couriers = getActiveCouriers();
        Courier selectedCourier;
        if (!couriers.isEmpty()) {
            // Pick a courier (prefer Delhivery for India, DTDC for others, or first available)
            selectedCourier = couriers.stream()
                    .filter(c -> "DELHIVERY".equals(c.getCode()))
                    .findFirst()
                    .orElse(couriers.get(0));
        } else {
            selectedCourier = Courier.builder()
                    .name("PVR Courier")
                    .code("PVR_COURIER")
                    .build();
        }

        // Generate mock tracking number: PVR-123456789
        long trackingNum = 100000000L + random.nextInt(900000000);
        String trackingId = "PVR" + trackingNum;

        order.setCourierName(selectedCourier.getName());
        order.setTrackingNumber(trackingId);

        return selectedCourier.getName();
    }
}
