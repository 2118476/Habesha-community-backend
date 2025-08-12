package com.habesha.community.dto;

import com.habesha.community.model.ContactRequestStatus;
import com.habesha.community.model.ContactType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * A simple DTO for transferring contact request information to the
 * frontend.  It includes identifiers for the requester and target,
 * their display names, the type of contact requested (email or
 * phone), the current status, and when the request was created.
 */
@Data
@Builder
public class ContactRequestDto {
    private Long id;
    private Long requesterId;
    private String requesterName;
    private Long targetId;
    private String targetName;
    private ContactType type;
    private ContactRequestStatus status;
    private LocalDateTime createdAt;
}