package com.hypercell.event_ticketing_platform.Entity;

import com.hypercell.event_ticketing_platform.Enum.SeatCategoryName;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "seats", uniqueConstraints = {
    @UniqueConstraint(name = "uk_seats_venue_row_number", columnNames = {"venue_id", "row_name", "seat_number"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeatEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venue_id", nullable = false)
    private VenueEntity venue;

    @Column(name = "row_name", nullable = false, length = 10)
    private String rowName;

    @Column(name = "seat_number", nullable = false)
    private Integer seatNumber;

    @Column(name = "seat_code", nullable = false, length = 20)
    private String seatCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "category_name", nullable = false, length = 50)
    @Builder.Default
    private SeatCategoryName categoryName = SeatCategoryName.STANDARD;
}
