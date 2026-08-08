package com.hypercell.event_ticketing_platform.Service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hypercell.event_ticketing_platform.DTO.UserManagementDto;
import com.hypercell.event_ticketing_platform.Entity.UserEntity;
import com.hypercell.event_ticketing_platform.Enum.UserRole;
import com.hypercell.event_ticketing_platform.Exception.ResourceNotFoundException;
import com.hypercell.event_ticketing_platform.Repository.UserRepository;

/**
 * Business Logic Service for Admin User Management.
 * Handles fetching user rosters and updating user permissions safely within transactional boundaries.
 */
@Service
public class UserManagementService {

    private final UserRepository userRepository;
    private final com.hypercell.event_ticketing_platform.Repository.BookingRepository bookingRepository;
    private final com.hypercell.event_ticketing_platform.Repository.TicketRepository ticketRepository;

    public UserManagementService(UserRepository userRepository,
                                 com.hypercell.event_ticketing_platform.Repository.BookingRepository bookingRepository,
                                 com.hypercell.event_ticketing_platform.Repository.TicketRepository ticketRepository) {
        this.userRepository = userRepository;
        this.bookingRepository = bookingRepository;
        this.ticketRepository = ticketRepository;
    }

    /**
     * Fetches all registered users from persistence and converts them to secure Response DTOs.
     * 
     * @return List of UserManagementDto.Response containing safe user metadata.
     */
    @Transactional(readOnly = true)
    public List<UserManagementDto.Response> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Updates the access role of a specified user entity.
     * 
     * Edge Case & Guard Prevention:
     * Validates that the active admin username/email does not match the target user ID,
     * protecting the platform against accidental self-demotion or self-lockout.
     * 
     * @param targetUserId ID of the target user to modify.
     * @param request DTO payload containing the new role string.
     * @param currentAdminUsername Email or username of the authenticated admin performing the operation.
     * @return Updated user details represented as UserManagementDto.Response.
     */
    @Transactional
    public UserManagementDto.Response changeUserRole(Long targetUserId, UserManagementDto.Request request, String currentAdminUsername) {
        // 1. Retrieve target user entity or throw ResourceNotFoundException
        UserEntity targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + targetUserId));

        // 2. Edge-Case Prevention: Block admin self-demotion
        if (currentAdminUsername != null &&
           (currentAdminUsername.equalsIgnoreCase(targetUser.getUsername()) || currentAdminUsername.equalsIgnoreCase(targetUser.getEmail()))) {
            throw new IllegalArgumentException("Action Denied: Admin cannot modify their own role to prevent accidental self-demotion.");
        }

        // 3. Convert incoming role string to valid UserRole enum
        UserRole newRoleEnum;
        try {
            newRoleEnum = UserRole.valueOf(request.getNewRole().trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid user role: '" + request.getNewRole() + "'. Accepted values are: CUSTOMER, ORGANIZER, ADMIN");
        }

        // 4. Update role and save entity within transactional context
        targetUser.setRole(newRoleEnum);
        UserEntity updatedUser = userRepository.save(targetUser);

        // 5. Map and return response DTO
        return mapToResponse(updatedUser);
    }

    /**
     * Deletes a user entity from persistence.
     * Prevents admin self-deletion.
     */
    @Transactional
    public void deleteUser(Long targetUserId, String currentAdminUsername) {
        UserEntity targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + targetUserId));

        if (currentAdminUsername != null &&
           (currentAdminUsername.equalsIgnoreCase(targetUser.getUsername()) || currentAdminUsername.equalsIgnoreCase(targetUser.getEmail()))) {
            throw new IllegalArgumentException("Action Denied: Admin cannot delete their own account.");
        }

        // Clean up linked tickets and bookings to prevent FK violation
        var tickets = ticketRepository.findByBookingUserId(targetUserId);
        if (tickets != null && !tickets.isEmpty()) {
            ticketRepository.deleteAll(tickets);
        }

        var bookings = bookingRepository.findByUserId(targetUserId);
        if (bookings != null && !bookings.isEmpty()) {
            bookingRepository.deleteAll(bookings);
        }

        userRepository.delete(targetUser);
    }

    /**
     * Private mapping utility converting UserEntity to UserManagementDto.Response.
     */
    private UserManagementDto.Response mapToResponse(UserEntity user) {
        return UserManagementDto.Response.builder()
                .id(user.getId())
                .name(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }
}
