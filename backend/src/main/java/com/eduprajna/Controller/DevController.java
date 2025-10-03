package com.eduprajna.Controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.eduprajna.entity.User;
import com.eduprajna.service.UserService;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Map;

/**
 * Development controller for seeding the database with sample data
 * WARNING: Only use in development/testing environments
 */
@RestController
@RequestMapping("/api/dev")
@CrossOrigin(origins = {"http://localhost:3000", "http://127.0.0.1:3000", "https://nishmitha-roots.vercel.app"}, allowCredentials = "true")
public class DevController {
    
    private static final Logger logger = LoggerFactory.getLogger(DevController.class);
    
    private final UserService userService;
    
    public DevController(UserService userService) {
        this.userService = userService;
    }
    
    /**
     * Seed the database with sample users from the original MySQL dump
     * This endpoint creates the admin and test user accounts
     */
    @PostMapping("/seed-users")
    public ResponseEntity<?> seedUsers() {
        try {
            logger.info("Starting database seeding with sample users...");
            
            // Check if admin user already exists
            if (userService.findByEmail("admin@gmail.com").isPresent()) {
                logger.info("Sample users already exist, skipping seeding");
                return ResponseEntity.ok(Map.of(
                    "message", "Sample users already exist",
                    "admin_email", "admin@gmail.com",
                    "user_email", "nishu@gmail.com"
                ));
            }
            
            // Create admin user (from MySQL dump)
            User admin = new User();
            admin.setEmail("admin@gmail.com");
            admin.setName("Admin");
            admin.setPasswordHash("Admin@123"); // In production, this should be hashed
            admin.setPhone("8197277941");
            admin.setRole("admin");
            admin.setIsActive(true);
            admin.setLoyaltyPoints(0);
            admin.setTotalOrders(0);
            admin.setMemberSince(LocalDate.of(2025, 9, 24));
            admin.setCreatedAt(OffsetDateTime.of(2025, 9, 24, 10, 35, 10, 0, OffsetDateTime.now().getOffset()));
            admin.setUpdatedAt(OffsetDateTime.of(2025, 9, 24, 10, 35, 10, 0, OffsetDateTime.now().getOffset()));
            admin.setLastPasswordChange(OffsetDateTime.of(2025, 9, 24, 10, 35, 10, 0, OffsetDateTime.now().getOffset()));
            
            // Create test user (from MySQL dump)
            User testUser = new User();
            testUser.setEmail("nishu@gmail.com");
            testUser.setName("SS");
            testUser.setPasswordHash("Nishu@123"); // In production, this should be hashed
            testUser.setPhone("8197277941");
            testUser.setRole("user");
            testUser.setGender("Female");
            testUser.setDateOfBirth(LocalDate.of(2001, 7, 18));
            testUser.setIsActive(true);
            testUser.setLoyaltyPoints(0);
            testUser.setTotalOrders(3);
            testUser.setMemberSince(LocalDate.of(2025, 9, 24));
            testUser.setCreatedAt(OffsetDateTime.of(2025, 9, 24, 5, 10, 28, 779408000, OffsetDateTime.now().getOffset()));
            testUser.setUpdatedAt(OffsetDateTime.of(2025, 9, 29, 11, 17, 10, 416344000, OffsetDateTime.now().getOffset()));
            testUser.setLastPasswordChange(OffsetDateTime.of(2025, 9, 27, 7, 33, 58, 236364000, OffsetDateTime.now().getOffset()));
            
            // Save users
            User savedAdmin = userService.save(admin);
            User savedUser = userService.save(testUser);
            
            logger.info("Successfully seeded database with {} users", 2);
            
            return ResponseEntity.ok(Map.of(
                "message", "Database seeded successfully",
                "users_created", 2,
                "admin_user", Map.of(
                    "id", savedAdmin.getId(),
                    "email", savedAdmin.getEmail(),
                    "role", savedAdmin.getRole()
                ),
                "test_user", Map.of(
                    "id", savedUser.getId(),
                    "email", savedUser.getEmail(),
                    "role", savedUser.getRole()
                )
            ));
            
        } catch (Exception e) {
            logger.error("Error seeding database", e);
            return ResponseEntity.status(500).body(Map.of(
                "error", "Failed to seed database",
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Get current database status
     */
    @GetMapping("/status")
    public ResponseEntity<?> getDatabaseStatus() {
        try {
            long userCount = userService.count();
            boolean hasAdmin = userService.findByEmail("admin@gmail.com").isPresent();
            boolean hasTestUser = userService.findByEmail("nishu@gmail.com").isPresent();
            
            return ResponseEntity.ok(Map.of(
                "total_users", userCount,
                "has_admin_user", hasAdmin,
                "has_test_user", hasTestUser,
                "database_ready", hasAdmin && hasTestUser
            ));
        } catch (Exception e) {
            logger.error("Error checking database status", e);
            return ResponseEntity.status(500).body(Map.of(
                "error", "Failed to check database status",
                "message", e.getMessage()
            ));
        }
    }
}