package com.habesha.community.repository;

import com.habesha.community.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findByDateGreaterThanEqualOrderByDateAsc(java.time.LocalDate date);

    /**
     * Count how many events have been organised by a particular user.  Use nested property reference (`organizer.id`).
     */
    long countByOrganizer_Id(Long organizerId);
}