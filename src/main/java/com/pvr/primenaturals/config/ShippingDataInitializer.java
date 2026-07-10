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
import org.springframework.jdbc.core.JdbcTemplate;

@Component
public class ShippingDataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(ShippingDataInitializer.class);

    private final WarehouseRepository warehouseRepository;
    private final ShippingRuleRepository shippingRuleRepository;
    private final CourierRepository courierRepository;
    private final com.pvr.primenaturals.repository.CompanyLocationRepository companyLocationRepository;
    private final com.pvr.primenaturals.repository.SocialLinkRepository socialLinkRepository;
    private final com.pvr.primenaturals.repository.FAQRepository faqRepository;
    private final com.pvr.primenaturals.repository.BoardMemberRepository boardMemberRepository;
    private final JdbcTemplate jdbcTemplate;

    public ShippingDataInitializer(WarehouseRepository warehouseRepository,
                                   ShippingRuleRepository shippingRuleRepository,
                                   CourierRepository courierRepository,
                                   com.pvr.primenaturals.repository.CompanyLocationRepository companyLocationRepository,
                                   com.pvr.primenaturals.repository.SocialLinkRepository socialLinkRepository,
                                   com.pvr.primenaturals.repository.FAQRepository faqRepository,
                                   com.pvr.primenaturals.repository.BoardMemberRepository boardMemberRepository,
                                   JdbcTemplate jdbcTemplate) {
        this.warehouseRepository = warehouseRepository;
        this.shippingRuleRepository = shippingRuleRepository;
        this.courierRepository = courierRepository;
        this.companyLocationRepository = companyLocationRepository;
        this.socialLinkRepository = socialLinkRepository;
        this.faqRepository = faqRepository;
        this.boardMemberRepository = boardMemberRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("Starting Shipping & Delivery Promise engine data initialization...");

        // 1. Seed Warehouse
        if (warehouseRepository.count() == 0) {
            Warehouse mhbWh = Warehouse.builder()
                    .name("PVR Mahabubnagar Warehouse")
                    .locationCode("MHB_WH")
                    .address("H-No 22-127, Veera Shivaji Nagar, Badepalle, Jadcherla")
                    .city("Mahabubnagar")
                    .state("Telangana")
                    .country("IN")
                    .latitude(16.734)
                    .longitude(78.136)
                    .active(true)
                    .build();
            warehouseRepository.save(mhbWh);
            log.info("Seeded default warehouse: PVR Mahabubnagar Warehouse");
        }

        // 1b. Seed Company Locations
        if (companyLocationRepository.count() < 4) {
            companyLocationRepository.deleteAll();
            companyLocationRepository.save(com.pvr.primenaturals.entity.CompanyLocation.builder()
                    .name("Head Office (Jadcherla)")
                    .address("H-No 22-127, Veera Shivaji Nagar, Badepalle, Jadcherla, Mahabubnagar, Telangana - 509301")
                    .phone("+91 98491 08718")
                    .email("pvrprimenaturals@gmail.com")
                    .type("HQ")
                    .active(true)
                    .build());

            companyLocationRepository.save(com.pvr.primenaturals.entity.CompanyLocation.builder()
                    .name("Our Bengaluru Store")
                    .address("Nexus Hub, Indiranagar, Bengaluru, Karnataka 560038")
                    .phone("+91 98491 08718")
                    .email("pvrprimenaturals@gmail.com")
                    .type("BOUTIQUE")
                    .active(true)
                    .build());

            companyLocationRepository.save(com.pvr.primenaturals.entity.CompanyLocation.builder()
                    .name("Wayanad Spice Hub & Warehouse")
                    .address("45 Heritage Lane, Wayanad, Kerala 673121")
                    .phone("+91 98491 08718")
                    .email("pvrprimenaturals@gmail.com")
                    .type("WAREHOUSE")
                    .active(true)
                    .build());

            companyLocationRepository.save(com.pvr.primenaturals.entity.CompanyLocation.builder()
                    .name("Customer Help & Support")
                    .address("Online Help Desk & Mail Support")
                    .phone("+91 98491 08718")
                    .email("pvrprimenaturals@gmail.com")
                    .type("SUPPORT")
                    .active(true)
                    .build());
            
            log.info("Seeded default company locations (Head Office, Bengaluru Store, Wayanad Spice Hub, Support)");
        }

        // 1c. Seed Social Links
        if (socialLinkRepository.count() == 0) {
            socialLinkRepository.save(com.pvr.primenaturals.entity.SocialLink.builder()
                    .platform("INSTAGRAM")
                    .url("https://instagram.com")
                    .active(true)
                    .build());

            socialLinkRepository.save(com.pvr.primenaturals.entity.SocialLink.builder()
                    .platform("FACEBOOK")
                    .url("https://facebook.com")
                    .active(true)
                    .build());

            socialLinkRepository.save(com.pvr.primenaturals.entity.SocialLink.builder()
                    .platform("LINKEDIN")
                    .url("https://linkedin.com")
                    .active(true)
                    .build());

            socialLinkRepository.save(com.pvr.primenaturals.entity.SocialLink.builder()
                    .platform("WHATSAPP")
                    .url("https://wa.me/919849108718")
                    .active(true)
                    .build());

            socialLinkRepository.save(com.pvr.primenaturals.entity.SocialLink.builder()
                    .platform("YOUTUBE")
                    .url("https://youtube.com")
                    .active(true)
                    .build());

            socialLinkRepository.save(com.pvr.primenaturals.entity.SocialLink.builder()
                    .platform("TWITTER")
                    .url("https://twitter.com")
                    .active(true)
                    .build());

            log.info("Seeded default brand social links");
        }

        // 1d. Seed FAQs
        if (faqRepository.count() == 0) {
            faqRepository.save(com.pvr.primenaturals.entity.FAQ.builder()
                    .question("What is PVR Prime Naturals?")
                    .answer("PVR Prime Naturals is a premium brand offering artisanal food products, organic spices, and healthy combo packs sourced directly from farms and processed with zero chemical additives to preserve original flavor, nutrition, and quality.")
                    .displayOrder(1)
                    .active(true)
                    .build());

            faqRepository.save(com.pvr.primenaturals.entity.FAQ.builder()
                    .question("How do I check delivery availability in my area?")
                    .answer("Simply enter your 6-digit delivery pincode on any product detail page. The page will dynamically display shipping availability, estimated delivery days, and any applicable shipping charges.")
                    .displayOrder(2)
                    .active(true)
                    .build());

            faqRepository.save(com.pvr.primenaturals.entity.FAQ.builder()
                    .question("Do you offer Cash on Delivery (COD)?")
                    .answer("Yes, we offer Cash on Delivery (COD) on most serviceable pincodes for a small processing fee. You can select this option at the payment step of checkout.")
                    .displayOrder(3)
                    .active(true)
                    .build());

            faqRepository.save(com.pvr.primenaturals.entity.FAQ.builder()
                    .question("Can I cancel my order?")
                    .answer("You can cancel your order directly from the \"My Orders\" dashboard at any time before the status is updated to \"SHIPPED\". Once shipped, the order cannot be cancelled, but you can initiate a return within 7 days of delivery.")
                    .displayOrder(4)
                    .active(true)
                    .build());

            faqRepository.save(com.pvr.primenaturals.entity.FAQ.builder()
                    .question("How do your monthly subscriptions work?")
                    .answer("Our monthly subscriptions allow you to get recurring deliveries of your favorite products automatically (e.g., every month). You save extra on subscription purchases and can pause, skip, resume, or cancel anytime from your User Profile page under \"Subscriptions\".")
                    .displayOrder(5)
                    .active(true)
                    .build());

            faqRepository.save(com.pvr.primenaturals.entity.FAQ.builder()
                    .question("How do I use a coupon code?")
                    .answer("During checkout, in the order review section, enter your coupon code in the promo input box and click \"Apply\". The discount amount will be calculated and subtracted from your order total instantly.")
                    .displayOrder(6)
                    .active(true)
                    .build());

            faqRepository.save(com.pvr.primenaturals.entity.FAQ.builder()
                    .question("What should I do if my payment fails?")
                    .answer("If a payment fails during checkout, the checkout screen will offer a \"Retry Payment\" option. Rest assured, if your account is debited but the order is not confirmed, the money will be automatically refunded by your bank within 3–5 business days.")
                    .displayOrder(7)
                    .active(true)
                    .build());

            log.info("Seeded default frequently asked questions");
        }

        // 1e. Seed Board Members
        if (boardMemberRepository.count() == 0) {
            boardMemberRepository.save(com.pvr.primenaturals.entity.BoardMember.builder()
                    .name("Perumalla Venkat Reddy")
                    .role("Founder & Chairperson")
                    .initials("VR")
                    .bio("A visionary entrepreneur and investor, dedicated to reviving traditional food processing methods and sustainable agriculture.")
                    .displayOrder(1)
                    .active(true)
                    .build());

            boardMemberRepository.save(com.pvr.primenaturals.entity.BoardMember.builder()
                    .name("Perumalla Vijaya Laxmi")
                    .role("Managing Director & CEO")
                    .initials("VL")
                    .bio("Oversees processing operations, ensuring traditional stone-ground textures and raw aromas are preserved with zero compromise.")
                    .displayOrder(2)
                    .active(true)
                    .build());

            boardMemberRepository.save(com.pvr.primenaturals.entity.BoardMember.builder()
                    .name("Perumalla Pravallika")
                    .role("Director of Operations & Marketing Executive")
                    .initials("VP")
                    .bio("Drives brand communication and operations strategy, making premium purity accessible across India.")
                    .displayOrder(3)
                    .active(true)
                    .build());

            boardMemberRepository.save(com.pvr.primenaturals.entity.BoardMember.builder()
                    .name("Perumalla Pradeep Kumar Reddy")
                    .role("Director, Head of Manufacturing & Quality")
                    .initials("PR")
                    .bio("Manages factory production pipelines and quality controls to guarantee premium artisanal outcomes.")
                    .displayOrder(4)
                    .active(true)
                    .build());

            log.info("Seeded default board of directors members");
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

        // 4. Update legacy /images/ product URLs and S3 URLs to correct relative/endpoint paths in the DB
        try {
            jdbcTemplate.update("UPDATE products SET image_url = '/assets/products/turmeric.png' WHERE image_url = '/images/turmeric_powder.png'");
            jdbcTemplate.update("UPDATE products SET image_url = '/assets/products/chilli.png' WHERE image_url = '/images/kashmiri_chili.png'");
            jdbcTemplate.update("UPDATE products SET image_url = '/assets/products/pepper.png' WHERE image_url = '/images/black_pepper.png'");
            jdbcTemplate.update("UPDATE products SET image_url = '/assets/products/cardamom.png' WHERE image_url = '/images/cardamom.png'");
            jdbcTemplate.update("UPDATE products SET image_url = '/assets/products/coffee.png' WHERE image_url = '/images/malabar_coffee.png'");
            jdbcTemplate.update("UPDATE products SET image_url = '/assets/products/coffee.png' WHERE image_url = '/images/mysore_coffee.png'");
            jdbcTemplate.update("UPDATE products SET image_url = '/assets/products/pickle.png' WHERE image_url = '/images/mango_pickle.png'");
            jdbcTemplate.update("UPDATE products SET image_url = '/assets/products/dry_fruits.png' WHERE image_url = '/images/cashews.png'");
            jdbcTemplate.update("UPDATE products SET image_url = '/assets/products/dry_fruits.png' WHERE image_url = '/images/almonds.png'");
            jdbcTemplate.update("UPDATE products SET image_url = '/assets/products/dry_fruits.png' WHERE image_url = '/images/pistachios.png'");
            jdbcTemplate.update("UPDATE products SET image_url = '/assets/products/dry_fruits.png' WHERE image_url = '/images/medjool_dates.png'");
            // General fallback replacement for any other /images/ references
            jdbcTemplate.update("UPDATE products SET image_url = REPLACE(image_url, '/images/', '/assets/products/') WHERE image_url LIKE '/images/%'");
            // Update S3 direct URLs to use the secure /api/upload/files/ proxy endpoint
            jdbcTemplate.update("UPDATE products SET image_url = REPLACE(image_url, 'https://pvr-prime-naturals-app.s3.eu-north-1.amazonaws.com/', '/api/upload/files/') WHERE image_url LIKE 'https://pvr-prime-naturals-app.s3.eu-north-1.amazonaws.com/%'");
            jdbcTemplate.update("UPDATE board_members SET image_url = NULL WHERE image_url LIKE '%/files/%' OR image_url LIKE '%amazonaws.com%'");
            log.info("Migrated product and board member image_url paths in database to correct public folder and proxy paths");
        } catch (Exception e) {
            log.error("Failed to run product/board member image_url database path migrations: {}", e.getMessage());
        }
    }
}
