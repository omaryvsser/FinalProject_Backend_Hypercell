package com.hypercell.event_ticketing_platform.Controller;

import java.security.Principal;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hypercell.event_ticketing_platform.DTO.UserManagementDto;
import com.hypercell.event_ticketing_platform.Service.UserManagementService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class UserManagementController {

    private final UserManagementService userManagementService;

    public UserManagementController(UserManagementService userManagementService) {
        this.userManagementService = userManagementService;
    }

    @GetMapping
    public ResponseEntity<?> getAllUsers(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        if (page != null && size != null) {
            Page<UserManagementDto.Response> paginated = userManagementService.getPaginatedUsers(page, size);
            return ResponseEntity.ok(paginated);
        }
        List<UserManagementDto.Response> users = userManagementService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @PutMapping("/{id}/role")
    public ResponseEntity<UserManagementDto.Response> changeUserRole(
            @PathVariable Long id,
            @Valid @RequestBody UserManagementDto.Request request,
            Principal principal) {

        String currentAdminUsername = (principal != null) ? principal.getName() : null;
        UserManagementDto.Response updatedUser = userManagementService.changeUserRole(id, request, currentAdminUsername);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id, Principal principal) {
        String currentAdminUsername = (principal != null) ? principal.getName() : null;
        userManagementService.deleteUser(id, currentAdminUsername);
        return ResponseEntity.noContent().build();
    }
}
