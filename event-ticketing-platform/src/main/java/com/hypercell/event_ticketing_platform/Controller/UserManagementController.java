package com.hypercell.event_ticketing_platform.Controller;

import java.security.Principal;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hypercell.event_ticketing_platform.DTO.UserManagementDto;
import com.hypercell.event_ticketing_platform.Service.UserManagementService;

import jakarta.validation.Valid;

/**
 * REST Controller exposing administrative User Management endpoints.
 * Secured at class level using Spring Security @PreAuthorize to restrict access to ADMIN roles only.
 */
@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class UserManagementController {

    private final UserManagementService userManagementService;

    /**
     * Constructor injection for loose coupling and easy mock testing.
     */
    public UserManagementController(UserManagementService userManagementService) {
        this.userManagementService = userManagementService;
    }

    /**
     * GET /api/admin/users
     * Retrieves all registered system users.
     * 
     * Security Guard: Accessible only by ADMINs via class-level @PreAuthorize.
     * 
     * @return 200 OK with List of UserManagementDto.Response payloads.
     */
    @GetMapping
    public ResponseEntity<List<UserManagementDto.Response>> getAllUsers() {
        List<UserManagementDto.Response> users = userManagementService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    /**
     * PUT /api/admin/users/{id}/role
     * Updates the role of a specified user.
     * 
     * Security & Context Extraction:
     * Extracts the active admin's username/email from the Principal object to prevent
     * self-demotion edge-cases inside the service layer.
     * 
     * @param id Target User ID.
     * @param request Validated Request body containing newRole.
     * @param principal Currently authenticated security principal.
     * @return 200 OK with updated UserManagementDto.Response.
     */
    @PutMapping("/{id}/role")
    public ResponseEntity<UserManagementDto.Response> changeUserRole(
            @PathVariable Long id,
            @Valid @RequestBody UserManagementDto.Request request,
            Principal principal) {
        
        String currentAdminUsername = (principal != null) ? principal.getName() : null;
        UserManagementDto.Response updatedUser = userManagementService.changeUserRole(id, request, currentAdminUsername);
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * DELETE /api/admin/users/{id}
     * Deletes a user account.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id, Principal principal) {
        String currentAdminUsername = (principal != null) ? principal.getName() : null;
        userManagementService.deleteUser(id, currentAdminUsername);
        return ResponseEntity.noContent().build();
    }
}
