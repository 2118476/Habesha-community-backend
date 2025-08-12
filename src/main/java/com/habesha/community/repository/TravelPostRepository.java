package com.habesha.community.repository;

import com.habesha.community.model.TravelPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface TravelPostRepository extends JpaRepository<TravelPost, Long> {

    List<TravelPost> findByTravelDate(LocalDate travelDate);

    List<TravelPost> findByOriginCityIgnoreCaseAndDestinationCityIgnoreCase(String originCity, String destinationCity);

    @Query("""
        SELECT t
        FROM TravelPost t
        JOIN FETCH t.user u
        WHERE (:origin IS NULL OR LOWER(t.originCity) = LOWER(:origin))
          AND (:destination IS NULL OR LOWER(t.destinationCity) = LOWER(:destination))
          AND (:date IS NULL OR t.travelDate = :date)
        """)
    List<TravelPost> search(@Param("origin") String origin,
                            @Param("destination") String destination,
                            @Param("date") LocalDate date);
}
