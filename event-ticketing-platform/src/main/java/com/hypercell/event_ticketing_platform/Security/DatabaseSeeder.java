package com.hypercell.event_ticketing_platform.Security;

import com.hypercell.event_ticketing_platform.Entity.*;
import com.hypercell.event_ticketing_platform.Enum.*;
import com.hypercell.event_ticketing_platform.Repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class DatabaseSeeder {

    private final UserRepository userRepository;
    private final VenueRepository venueRepository;
    private final EventRepository eventRepository;
    private final SeatCategoryRepository seatCategoryRepository;
    private final BookingRepository bookingRepository;
    private final SeatRepository seatRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner seedDatabase() {
        return args -> {
            // ✅ Always ensure admin@hypercell.com has ADMIN role
            userRepository.findByEmail("admin@hypercell.com").ifPresentOrElse(
                user -> {
                    if (user.getRole() != UserRole.ADMIN) {
                        user.setRole(UserRole.ADMIN);
                        userRepository.save(user);
                        System.out.println("✅ Updated admin@hypercell.com role to ADMIN.");
                    }
                },
                () -> {
                    userRepository.save(UserEntity.builder()
                            .username("System Admin")
                            .email("admin@hypercell.com")
                            .password(passwordEncoder.encode("0000000000"))
                            .role(UserRole.ADMIN)
                            .build());
                    System.out.println("✅ Seeded admin@hypercell.com with ADMIN role.");
                }
            );

            // ✅ Always ensure organizer1@hypercell.com has ORGANIZER role
            userRepository.findByEmail("organizer1@hypercell.com").ifPresentOrElse(
                user -> {
                    if (user.getRole() != UserRole.ORGANIZER) {
                        user.setRole(UserRole.ORGANIZER);
                        userRepository.save(user);
                        System.out.println("✅ Updated organizer1@hypercell.com role to ORGANIZER.");
                    }
                },
                () -> {
                    userRepository.save(UserEntity.builder()
                            .username("Organizer One")
                            .email("organizer1@hypercell.com")
                            .password(passwordEncoder.encode("password123"))
                            .role(UserRole.ORGANIZER)
                            .build());
                    System.out.println("✅ Seeded organizer1@hypercell.com with ORGANIZER role.");
                }
            );

            // ✅ Skip rest if seed data already exists
            if (userRepository.existsByEmail("admin@ticketing.com")) {
                System.out.println("ℹ️ Seed data already exists, skipping database seeder.");
                return;
            }

            // 1. Seed Users (with BCrypt-encoded passwords)
            UserEntity admin = userRepository.save(UserEntity.builder()
                    .username("admin_user")
                    .email("admin@ticketing.com")
                    .password(passwordEncoder.encode("password123"))
                    .role(UserRole.ADMIN)
                    .build());

            UserEntity organizer1 = userRepository.save(UserEntity.builder()
                    .username("ahmed_organizer")
                    .email("ahmed@events.com")
                    .password(passwordEncoder.encode("password123"))
                    .role(UserRole.ORGANIZER)
                    .build());

            UserEntity customer1 = userRepository.save(UserEntity.builder()
                    .username("omar_customer")
                    .email("omar@gmail.com")
                    .password(passwordEncoder.encode("password123"))
                    .role(UserRole.CUSTOMER)
                    .build());

            // 2. Seed Venues
            VenueEntity venue1 = venueRepository.save(VenueEntity.builder()
                    .name("Cairo International Conference Center")
                    .address("Nasr City, Cairo")
                    .capacity(5000)
                    .build());

            VenueEntity venue2 = venueRepository.save(VenueEntity.builder()
                    .name("Al Manara Arts Center")
                    .address("New Cairo")
                    .capacity(2000)
                    .build());

            // 2.5 Seed Venue Seats
            List<SeatEntity> venue1Seats = seedVenueSeats(venue1);
            seedVenueSeats(venue2);

            // 3. Seed Events (with Image URLs)
            EventEntity event1 = eventRepository.save(EventEntity.builder()
                    .title("Tech Summit 2026")
                    .description("The biggest technology conference in the Middle East featuring top speakers.")
                    .category("Technology")
                    .startDate(LocalDateTime.now().plusDays(10))
                    .endDate(LocalDateTime.now().plusDays(12))
                    .status(EventStatus.PUBLISHED)
                    .organizer(organizer1)
                    .venue(venue1)
                    .imageUrl("https://images.unsplash.com/photo-1540575467063-178a50c2df87?w=800")
                    .build());

            EventEntity event2 = eventRepository.save(EventEntity.builder()
                    .title("Cairo Jazz Festival")
                    .description("An evening of world-class jazz performances featuring international and local artists.")
                    .category("Music")
                    .startDate(LocalDateTime.now().plusDays(20))
                    .endDate(LocalDateTime.now().plusDays(20).plusHours(4))
                    .status(EventStatus.PUBLISHED)
                    .organizer(organizer1)
                    .venue(venue2)
                    .imageUrl("https://images.unsplash.com/photo-1511192336575-5a79af67a629?w=800")
                    .build());

            // 4. Seed Seat Categories
            SeatCategoryEntity seatCat1 = seatCategoryRepository.save(SeatCategoryEntity.builder()
                    .name(SeatCategoryName.VIP)
                    .price(BigDecimal.valueOf(500.00))
                    .totalSeats(200)
                    .availableSeats(198)
                    .event(event1)
                    .build());

            SeatCategoryEntity seatCat2 = seatCategoryRepository.save(SeatCategoryEntity.builder()
                    .name(SeatCategoryName.STANDARD)
                    .price(BigDecimal.valueOf(150.00))
                    .totalSeats(1000)
                    .availableSeats(1000)
                    .event(event1)
                    .build());

            // 5. Seed Booking
            BookingEntity booking = BookingEntity.builder()
                    .bookingDate(LocalDateTime.now())
                    .status(BookingStatus.CONFIRMED)
                    .quantity(2)
                    .user(customer1)
                    .event(event1)
                    .seatCategory(seatCat1)
                    .build();

            if (venue1Seats.size() >= 2) {
                booking.addTicket(TicketEntity.builder()
                        .ticketNumber("TKN-SEED01-A1")
                        .ticketCode("TCK-QR-SEED01-A1")
                        .isBooked(true)
                        .seat(venue1Seats.get(0))
                        .build());
                booking.addTicket(TicketEntity.builder()
                        .ticketNumber("TKN-SEED02-A2")
                        .ticketCode("TCK-QR-SEED02-A2")
                        .isBooked(true)
                        .seat(venue1Seats.get(1))
                        .build());
            }

            bookingRepository.save(booking);

            System.out.println("✅ Database seeding completed successfully!");
        };
    }

    private List<SeatEntity> seedVenueSeats(VenueEntity venue) {
        if (seatRepository.existsByVenueId(venue.getId())) {
            return seatRepository.findByVenueIdOrderByRowNameAscSeatNumberAsc(venue.getId());
        }
        List<SeatEntity> list = new java.util.ArrayList<>();
        String[] rows = {"A", "B", "C", "D", "E", "F"};
        for (String r : rows) {
            SeatCategoryName cat = "F".equals(r) ? SeatCategoryName.VIP : ("E".equals(r) ? SeatCategoryName.IMAX : SeatCategoryName.STANDARD);
            for (int n = 1; n <= 8; n++) {
                list.add(SeatEntity.builder()
                        .venue(venue)
                        .rowName(r)
                        .seatNumber(n)
                        .seatCode(r + n)
                        .categoryName(cat)
                        .build());
            }
        }
        return seatRepository.saveAll(list);
    }
}