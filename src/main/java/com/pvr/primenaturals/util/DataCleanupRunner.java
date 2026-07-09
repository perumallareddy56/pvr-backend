package com.pvr.primenaturals.util;

import com.pvr.primenaturals.entity.User;
import com.pvr.primenaturals.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * One-time utility to remove test users from the database.
 * This runs automatically when the application starts in IntelliJ.
 */
@Component
public class DataCleanupRunner implements CommandLineRunner {

    private static final Logger log = Logger.getLogger(DataCleanupRunner.class.getName());

    @Autowired
    private UserRepository userRepository;

    @Override
    public void run(String... args) throws Exception {
        log.info("[CLEANUP] Initializing test data removal protocol...");
        List<String> emailsToRemove = Arrays.asList("testuser@pvr.com", "telemetry_test@pvr.com");

        for (String email : emailsToRemove) {
            Optional<User> user = userRepository.findByEmail(email);
            if (user.isPresent()) {
                try {
                    userRepository.delete(user.get());
                    log.info("[CLEANUP] Successfully removed test user: " + email);
                } catch (Exception e) {
                    log.warning("[CLEANUP] Could not remove " + email + ". It might be linked to existing orders. Error: " + e.getMessage());
                }
            } else {
                log.info("[CLEANUP] User not found or already removed: " + email);
            }
        }
        log.info("[CLEANUP] Protocol complete.");
    }
}
