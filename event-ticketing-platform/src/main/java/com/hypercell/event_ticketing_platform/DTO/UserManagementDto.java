package com.hypercell.event_ticketing_platform.DTO;

import com.hypercell.event_ticketing_platform.Enum.UserRole;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Single DTO wrapper containing static nested Request and Response payload classes
 * for User Management operations under the Admin module.
 * 
 * Strict Architectural Enforcement:
 * Combines Request and Response in a single outer wrapper class to streamline 
 * project structure and eliminate redundant DTO file proliferation.
 */
public class UserManagementDto {

    /**
     * Response payload used to expose user account details safely to Admin clients.
     * Sensitive fields like BCrypt password hashes are deliberately omitted to prevent security leaks.
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private Long id;
        private String name;
        private String email;
        private UserRole role;
    }

    /**
     * Request payload for modifying a user's role.
     * Enforces incoming data integrity using Spring Validation annotations.
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Request {

        @NotBlank(message = "New role must not be blank")
        private String newRole;
    }
}
