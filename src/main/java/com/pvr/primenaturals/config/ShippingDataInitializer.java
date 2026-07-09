package com.pvr.primenaturals.config;

import com.pvr.primenaturals.entity.Courier;
import com.pvr.primenaturals.entity.ShippingRule;
import com.pvr.primenaturals.entity.Warehouse;
import com.pvr.primenaturals.repository.CourierRepository;
import com.pvr.primenaturals.repository.ShippingRuleRepository;
import com.pvr.primenaturals.repository.WarehouseRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.util.List;

@Component
public class ShippingDataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(ShippingDataInitializer.class);

    private final WarehouseRepository warehouseRepository;
    private final ShippingRuleRepository shippingRuleRepository;
    private final CourierRepository courierRepository;

    public ShippingDataInitializer(WarehouseRepository warehouseRepository,
                                   ShippingRuleRepository shippingRuleRepository,
                                   CourierRepository courierRepository) {
        this.warehouseRepository = warehouseRepository;
        this.shippingRuleRepository = shippingRuleRepository;
        this.courierRepository = courierRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("Starting Shipping & Delivery Promise engine data initialization...");

        // 1. Seed Warehouse
        if (warehouseRepository.count() == 0) {
            Warehouse hydWh = Warehouse.builder()
                    .name("PVR Hyderabad Warehouse")
                    .locationCode("HYD_WH")
                    .address("KPHB Colony, Hyderabad")
                    .city("Hyderabad")
                    .state("Telangana")
                    .country("IN")
                    .latitude(17.484)
                    .longitude(78.388)
                    .active(true)
                    .build();
            warehouseRepository.save(hydWh);
            log.info("Seeded default warehouse: PVR Hyderabad Warehouse");
        } else {
            // Update the existing warehouses if they don't have city/state/country populated
            warehouseRepository.findAll().forEach(wh -> {
                if (wh.getCountry() == null) {
                    wh.setCity("Hyderabad");
                    wh.setState("Telangana");
                    wh.setCountry("IN");
                    wh.setLatitude(17.484);
                    wh.setLongitude(78.388);
                    wh.setActive(true);
                    warehouseRepository.save(wh);
                }
            });
        }

        // 2. Seed Shipping Rules
        if (shippingRuleRepository.count() == 0) {
            List<ShippingRule> defaultRules = List.of(
                    ShippingRule.builder()
                            .country("IN")
                            .minOrder(BigDecimal.ZERO)
                            .maxWeight(100.0)
                            .shippingCharge(new BigDecimal("60.00"))
                            .freeShippingAbove(new BigDecimal("499.00"))
                            .active(true)
                            .build(),
                    ShippingRule.builder()
                            .country("US")
                            .minOrder(BigDecimal.ZERO)
                            .maxWeight(10.0)
                            .shippingCharge(new BigDecimal("15.00"))
                            .freeShippingAbove(new BigDecimal("100.00"))
                            .active(true)
                            .build()
            );
            shippingRuleRepository.saveAll(defaultRules);
            log.info("Seeded default shipping rules for IN and US");
        }

        // 3. Seed Couriers
        if (courierRepository.count() == 0) {
            List<Courier> defaultCouriers = List.of(
                    Courier.builder()
                            .name("Delhivery")
                            .code("DELHIVERY")
                            .trackingUrlTemplate("https://www.delhivery.com/track/package/{trackingId}")
                            .active(true)
                            .build(),
                    Courier.builder()
                            .name("Blue Dart")
                            .code("BLUEDART")
                            .trackingUrlTemplate("https://www.bluedart.com/tracking/{trackingId}")
                            .active(true)
                            .build(),
                    Courier.builder()
                            .name("DTDC")
                            .code("DTDC")
                            .trackingUrlTemplate("https://www.dtdc.in/tracking/{trackingId}")
                            .active(true)
                            .build(),
                    Courier.builder()
                            .name("Mock Courier")
                            .code("MOCK")
                            .trackingUrlTemplate("https://pvrprimenaturals.com/track/{trackingId}")
                            .active(true)
                            .build()
            );
            courierRepository.saveAll(defaultCouriers);
            log.info("Seeded default couriers");
        }
    }
}
