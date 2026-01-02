package com.habesha.community.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Response payload for an event detail.  Contains the event's
 * properties as well as a summary of the author (organiser).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventDetailDto {
    private Long id;
    private String title;
    private String description;
    private String category;
    private LocalDate date;
    private String location;
    private List<String> images;
    private LocalDateTime createdAt;
    /**
     * Summary of the user who posted this event.  Previously exposed
     * under the {@code author} field, this new property aligns
     * consistently with other entity responses.  The existing
     * {@code author} property remains for backwards compatibility but
     * will be deprecated in future releases.
     */
    private UserSummaryDto postedBy;

    /**
     * @deprecated use {@link #postedBy} instead.  Retained to avoid
     * breaking existing clients.
     */
    @Deprecated
    private UserSummaryDto author;
}