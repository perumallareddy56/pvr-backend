package com.pvr.primenaturals.security;

import com.pvr.primenaturals.entity.Role;
import com.pvr.primenaturals.entity.User;
import com.pvr.primenaturals.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class SecurityDataInitializer implements CommandLineRunner {

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(SecurityDataInitializer.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${app.admin.email:admin@gmail.com}")
    private String adminEmail;

    @Value("${app.admin.password:admin123}")
    private String adminPass;

    @Override
    public void run(String... args) throws Exception {
        if (!userRepository.existsByEmail(adminEmail)) {
            User admin = User.builder()
                    .name("Admin")
                    .email(adminEmail)
                    .password(passwordEncoder.encode(adminPass))
                    .role(Role.ADMIN)
                    .build();
            userRepository.save(admin);
            logger.info("Default Admin User created: " + adminEmail);
        } else {
            // Ensure the admin user has the correct role
            User admin = userRepository.findByEmail(adminEmail).get();
            if (admin.getRole() != Role.ADMIN) {
                admin.setRole(Role.ADMIN);
                userRepository.save(admin);
                logger.debug("Admin role restored for: " + adminEmail);
            }
            logger.info("Default Admin User already exists: " + adminEmail);
        }
    }
}
