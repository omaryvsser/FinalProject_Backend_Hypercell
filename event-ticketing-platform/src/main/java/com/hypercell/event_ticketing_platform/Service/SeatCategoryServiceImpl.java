package com.hypercell.event_ticketing_platform.Service;

import com.hypercell.event_ticketing_platform.DTO.SeatCategoryDto;
import com.hypercell.event_ticketing_platform.Entity.EventEntity;
import com.hypercell.event_ticketing_platform.Entity.SeatCategoryEntity;
import com.hypercell.event_ticketing_platform.Entity.UserEntity;
import com.hypercell.event_ticketing_platform.Exception.ResourceAlreadyExistsException;
import com.hypercell.event_ticketing_platform.Exception.ResourceNotFoundException;
import com.hypercell.event_ticketing_platform.Repository.EventRepository;
import com.hypercell.event_ticketing_platform.Repository.SeatCategoryRepository;
import com.hypercell.event_ticketing_platform.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SeatCategoryServiceImpl implements SeatCategoryService {

    private final SeatCategoryRepository seatCategoryRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public SeatCategoryDto.Response addSeatCategory(Long eventId, SeatCategoryDto.CreateRequest createSeatCategoryDto) {
        UserEntity currentUser = getAuthenticatedUser();

        EventEntity event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + eventId));

        verifyOwnership(event, currentUser);

        if (createSeatCategoryDto.getEventId() != null && !createSeatCategoryDto.getEventId().equals(eventId)) {
            throw new IllegalArgumentException("The eventId in the URL path (" + eventId + ") does not match the eventId in the request body (" + createSeatCategoryDto.getEventId() + ")");
        }

        if (seatCategoryRepository.existsByEventIdAndName(eventId, createSeatCategoryDto.getName())) {
            throw new ResourceAlreadyExistsException("Seat category '" + createSeatCategoryDto.getName() + "' already exists for event ID: " + eventId);
        }

        SeatCategoryEntity seatCategory = SeatCategoryEntity.builder()
                .name(createSeatCategoryDto.getName())
                .price(createSeatCategoryDto.getPrice())
                .totalSeats(createSeatCategoryDto.getTotalSeats())
                .availableSeats(createSeatCategoryDto.getTotalSeats())
                .event(event)
                .build();

        SeatCategoryEntity savedSeatCategory = seatCategoryRepository.save(seatCategory);
        return mapToSeatCategoryResponseDto(savedSeatCategory);
    }

    @Override
    @Transactional
    public SeatCategoryDto.Response updateSeatCategory(Long eventId, Long categoryId, SeatCategoryDto.UpdateRequest updateSeatCategoryDto) {
        UserEntity currentUser = getAuthenticatedUser();

        EventEntity event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + eventId));

        verifyOwnership(event, currentUser);

        SeatCategoryEntity seatCategory = seatCategoryRepository.findByIdAndEventId(categoryId, eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Seat category with id " + categoryId + " not found for event " + eventId));

        if (updateSeatCategoryDto.getName() != null && !updateSeatCategoryDto.getName().equals(seatCategory.getName())) {
            if (seatCategoryRepository.existsByEventIdAndName(eventId, updateSeatCategoryDto.getName())) {
                throw new ResourceAlreadyExistsException("Seat category '" + updateSeatCategoryDto.getName() + "' already exists for event ID: " + eventId);
            }
            seatCategory.setName(updateSeatCategoryDto.getName());
        }
        if (updateSeatCategoryDto.getPrice() != null) {
            seatCategory.setPrice(updateSeatCategoryDto.getPrice());
        }
        if (updateSeatCategoryDto.getTotalSeats() != null) {
            int currentTotal = seatCategory.getTotalSeats();
            int currentAvailable = seatCategory.getAvailableSeats();
            int bookedSeats = currentTotal - currentAvailable;
            int newTotal = updateSeatCategoryDto.getTotalSeats();

            if (newTotal < bookedSeats) {
                throw new IllegalArgumentException("Cannot reduce total seats below already booked count of " + bookedSeats);
            }

            seatCategory.setTotalSeats(newTotal);
            seatCategory.setAvailableSeats(newTotal - bookedSeats);
        }

        SeatCategoryEntity updatedSeatCategory = seatCategoryRepository.save(seatCategory);
        return mapToSeatCategoryResponseDto(updatedSeatCategory);
    }

    @Override
    @Transactional
    public void deleteSeatCategory(Long eventId, Long categoryId) {
        UserEntity currentUser = getAuthenticatedUser();

        EventEntity event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + eventId));

        verifyOwnership(event, currentUser);

        SeatCategoryEntity seatCategory = seatCategoryRepository.findByIdAndEventId(categoryId, eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Seat category with id " + categoryId + " not found for event " + eventId));

        seatCategoryRepository.delete(seatCategory);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SeatCategoryDto.Response> getSeatCategoriesByEventId(Long eventId) {
        if (!eventRepository.existsById(eventId)) {
            throw new ResourceNotFoundException("Event not found with id: " + eventId);
        }

        return seatCategoryRepository.findByEventId(eventId).stream()
                .map(this::mapToSeatCategoryResponseDto)
                .toList();
    }

    private UserEntity getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("User is not authenticated");
        }
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found with email: " + email));
    }

    private void verifyOwnership(EventEntity event, UserEntity currentUser) {
        if (!event.getOrganizer().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You are not authorized to manage seat categories for this event");
        }
    }

    private SeatCategoryDto.Response mapToSeatCategoryResponseDto(SeatCategoryEntity seatCategory) {
        return SeatCategoryDto.Response.builder()
                .id(seatCategory.getId())
                .name(seatCategory.getName())
                .price(seatCategory.getPrice())
                .totalSeats(seatCategory.getTotalSeats())
                .availableSeats(seatCategory.getAvailableSeats())
                .eventId(seatCategory.getEvent().getId())
                .build();
    }
}
