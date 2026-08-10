package com.hypercell.event_ticketing_platform.Service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hypercell.event_ticketing_platform.DTO.UserManagementDto;
import com.hypercell.event_ticketing_platform.Entity.UserEntity;
import com.hypercell.event_ticketing_platform.Enum.UserRole;
import com.hypercell.event_ticketing_platform.Exception.ResourceNotFoundException;
import com.hypercell.event_ticketing_platform.Repository.UserRepository;

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

    @Transactional(readOnly = true)
    public List<UserManagementDto.Response> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<UserManagementDto.Response> getPaginatedUsers(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return userRepository.findAll(pageable).map(this::mapToResponse);
    }

    @Transactional
    public UserManagementDto.Response changeUserRole(Long targetUserId, UserManagementDto.Request request, String currentAdminUsername) {
        UserEntity targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + targetUserId));

        if (currentAdminUsername != null &&
        (currentAdminUsername.equalsIgnoreCase(targetUser.getUsername()) || currentAdminUsername.equalsIgnoreCase(targetUser.getEmail()))) {
            throw new IllegalArgumentException("Action Denied: Admin cannot modify their own role to prevent accidental self-demotion.");
        }

        if (request == null || request.getNewRole() == null || request.getNewRole().trim().isEmpty()) {
            throw new IllegalArgumentException("New role must not be blank");
        }

        String rawRole = request.getNewRole().trim().toUpperCase();
        if (rawRole.startsWith("ROLE_")) {
            rawRole = rawRole.substring(5);
        }

        UserRole newRoleEnum;
        try {
            newRoleEnum = UserRole.valueOf(rawRole);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid user role: '" + request.getNewRole() + "'. Accepted values are: CUSTOMER, ORGANIZER, ADMIN");
        }

        targetUser.setRole(newRoleEnum);
        UserEntity updatedUser = userRepository.save(targetUser);

        return mapToResponse(updatedUser);
    }

    @Transactional
    public void deleteUser(Long targetUserId, String currentAdminUsername) {
        UserEntity targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + targetUserId));

        if (currentAdminUsername != null &&
        (currentAdminUsername.equalsIgnoreCase(targetUser.getUsername()) || currentAdminUsername.equalsIgnoreCase(targetUser.getEmail()))) {
            throw new IllegalArgumentException("Action Denied: Admin cannot delete their own account.");
        }

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

    private UserManagementDto.Response mapToResponse(UserEntity user) {
        return UserManagementDto.Response.builder()
                .id(user.getId())
                .name(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }
}
