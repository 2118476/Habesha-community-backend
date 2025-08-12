package com.habesha.community.service;

import com.habesha.community.dto.EventRequest;
import com.habesha.community.model.Event;
import com.habesha.community.model.Role;
import com.habesha.community.model.User;
import com.habesha.community.repository.EventRepository;
import com.habesha.community.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * Service for posting and managing events.
 */
@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userRepository.findByEmail(auth.getName()).orElseThrow(() -> new IllegalStateException("No current user"));
    }

    @Transactional
    public Event createEvent(EventRequest request) {
        User organizer = getCurrentUser();
        Event event = Event.builder()
                .organizer(organizer)
                .title(request.getTitle())
                .date(request.getDate())
                .description(request.getDescription())
                .imageUrl(request.getImageUrl())
                .location(request.getLocation())
                .featured(request.isFeatured())
                .verified(false)
                .build();
        return eventRepository.save(event);
    }

    public List<Event> listUpcomingEvents() {
        // Return events from today onwards sorted by date
        return eventRepository.findByDateGreaterThanEqualOrderByDateAsc(LocalDate.now());
    }

    @Transactional
    public void promoteEvent(Long id) {
        // Only the organizer or an admin should be able to promote; assume payment processed separately
        User current = getCurrentUser();
        Event event = eventRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Event not found"));
        if (!event.getOrganizer().getId().equals(current.getId()) && current.getRole() != Role.ADMIN) {
            throw new IllegalStateException("Not authorised to promote this event");
        }
        event.setFeatured(true);
        eventRepository.save(event);
    }

    @Transactional
    public void deleteEvent(Long id) {
        User current = getCurrentUser();
        Event event = eventRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Event not found"));
        if (!event.getOrganizer().getId().equals(current.getId()) && current.getRole() != Role.ADMIN) {
            throw new IllegalStateException("Not authorised to delete this event");
        }
        eventRepository.delete(event);
    }

    @Transactional
    public void verifyEvent(Long id) {
        // Only admin can verify
        User current = getCurrentUser();
        if (current.getRole() != Role.ADMIN) {
            throw new IllegalStateException("Only admin can verify events");
        }
        Event event = eventRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Event not found"));
        event.setVerified(true);
        eventRepository.save(event);
    }
}