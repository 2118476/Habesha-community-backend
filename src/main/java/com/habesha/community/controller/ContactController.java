package com.habesha.community.controller;

import com.habesha.community.dto.ContactRequestCreateRequest;
import com.habesha.community.service.ContactService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/contact")
@RequiredArgsConstructor
public class ContactController {
    private final ContactService contactService;

    @PostMapping("/request")
    public ResponseEntity<?> create(@Valid @RequestBody ContactRequestCreateRequest req) {
        contactService.create(req);
        return ResponseEntity.ok().build();
    }

    /**
     * Retrieve all pending contact requests directed at the
     * currently authenticated user.  These are requests that the
     * user may choose to approve or reject.  The caller must be
     * authenticated.
     */
    @GetMapping("/requests/incoming")
    public ResponseEntity<java.util.List<com.habesha.community.dto.ContactRequestDto>> incoming() {
        return ResponseEntity.ok(contactService.getIncomingForCurrentUser());
    }

    /**
     * Retrieve all contact requests initiated by the current user.
     * Includes pending, approved and rejected requests.  Useful for
     * showing request status to the requester.
     */
    @GetMapping("/requests/outgoing")
    public ResponseEntity<java.util.List<com.habesha.community.dto.ContactRequestDto>> outgoing() {
        return ResponseEntity.ok(contactService.getOutgoingForCurrentUser());
    }

    /**
     * Respond to a pending contact request.  Only the target user
     * (recipient) may call this endpoint.  Provide an 'accept'
     * boolean query parameter to indicate whether the request should
     * be approved (true) or rejected (false).
     */
    @PostMapping("/requests/{id}/respond")
    public ResponseEntity<Void> respond(@PathVariable Long id, @RequestParam boolean accept) {
        contactService.respond(id, accept);
        return ResponseEntity.ok().build();
    }
}
