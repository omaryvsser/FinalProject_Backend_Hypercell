package com.hypercell.event_ticketing_platform.DTO;

import com.hypercell.event_ticketing_platform.Enum.EventStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChangeEventStatusDto {

    @NotNull(message = "Event status is required")
    private EventStatus status;
}
