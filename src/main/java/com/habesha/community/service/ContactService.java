package com.habesha.community.service;

import com.habesha.community.dto.ContactRequestCreateRequest;
import com.habesha.community.model.*;
import com.habesha.community.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ContactService {
    private final ContactRequestRepository contactRepo;
    private final UserRepository userRepo;

    private User currentUser() {
        var email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepo.findByEmail(email).orElseThrow(() -> new IllegalStateException("No current user"));
    }

    public void create(ContactRequestCreateRequest req) {
        var me = currentUser();
        var target = userRepo.findById(req.getTargetUserId())
                .orElseThrow(() -> new IllegalArgumentException("Target user not found"));

        var type = switch (req.getType().toLowerCase()) {
            case "email" -> ContactType.EMAIL;
            case "phone" -> ContactType.PHONE;
            default -> throw new IllegalArgumentException("Invalid type");
        };

        // prevent duplicates while pending
        boolean exists = contactRepo.existsByRequesterIdAndTargetIdAndTypeAndStatus(
                me.getId(), target.getId(), type, ContactRequestStatus.PENDING);
        if (exists) return;

        var cr = ContactRequest.builder()
                .requester(me).target(target).type(type)
                .status(ContactRequestStatus.PENDING)
                .build();
        contactRepo.save(cr);
    }

    /**
     * Return all pending contact requests where the current user is the
     * target.  These represent incoming requests that the user needs
     * to approve or reject.  Results are mapped into simple DTO
     * objects containing only the information needed on the frontend.
     */
    public java.util.List<com.habesha.community.dto.ContactRequestDto> getIncomingForCurrentUser() {
        var me = currentUser();
        var list = contactRepo.findByTargetIdAndStatus(me.getId(), ContactRequestStatus.PENDING);
        return list.stream().map(req -> com.habesha.community.dto.ContactRequestDto.builder()
                .id(req.getId())
                .requesterId(req.getRequester().getId())
                .requesterName(req.getRequester().getName() != null && !req.getRequester().getName().isBlank()
                        ? req.getRequester().getName()
                        : req.getRequester().getUsername())
                .targetId(req.getTarget().getId())
                .targetName(me.getName() != null && !me.getName().isBlank() ? me.getName() : me.getUsername())
                .type(req.getType())
                .status(req.getStatus())
                .createdAt(req.getCreatedAt())
                .build()).collect(java.util.stream.Collectors.toList());
    }

    /**
     * Return all contact requests initiated by the current user.  This
     * includes pending, approved and rejected requests.  The DTO
     * contains both the requester (current user) and target
     * information for convenience.
     */
    public java.util.List<com.habesha.community.dto.ContactRequestDto> getOutgoingForCurrentUser() {
        var me = currentUser();
        var list = contactRepo.findByRequesterId(me.getId());
        return list.stream().map(req -> com.habesha.community.dto.ContactRequestDto.builder()
                .id(req.getId())
                .requesterId(me.getId())
                .requesterName(me.getName() != null && !me.getName().isBlank() ? me.getName() : me.getUsername())
                .targetId(req.getTarget().getId())
                .targetName(req.getTarget().getName() != null && !req.getTarget().getName().isBlank()
                        ? req.getTarget().getName()
                        : req.getTarget().getUsername())
                .type(req.getType())
                .status(req.getStatus())
                .createdAt(req.getCreatedAt())
                .build()).collect(java.util.stream.Collectors.toList());
    }

    /**
     * Respond to a contact request by either accepting or rejecting
     * it.  Only the target (recipient) of the request may respond.
     * When accepted, the status is set to APPROVED; when rejected,
     * the status is set to REJECTED.  If the request is not pending
     * or does not belong to the current user as the target, an
     * exception is thrown.
     */
    public void respond(Long requestId, boolean accept) {
        var reqOpt = contactRepo.findById(requestId);
        if (reqOpt.isEmpty()) {
            throw new IllegalArgumentException("Contact request not found");
        }
        var req = reqOpt.get();
        var me = currentUser();
        // Ensure only target can respond
        if (!req.getTarget().getId().equals(me.getId())) {
            throw new IllegalStateException("You are not authorized to respond to this request");
        }
        // Only pending requests can be responded to
        if (req.getStatus() != ContactRequestStatus.PENDING) {
            return;
        }
        req.setStatus(accept ? ContactRequestStatus.APPROVED : ContactRequestStatus.REJECTED);
        contactRepo.save(req);
    }
}
