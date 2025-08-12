package com.habesha.community.controller;

import com.habesha.community.dto.TravelCreateRequest;
import com.habesha.community.dto.TravelPostResponse;
import com.habesha.community.service.TravelService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class TravelController {

    private final TravelService travelService;

    @PostMapping("/travel")
    public ResponseEntity<TravelPostResponse> create(@RequestBody TravelCreateRequest req) {
        return ResponseEntity.ok(travelService.create(req));
    }

    @GetMapping("/travel")
    public ResponseEntity<List<TravelPostResponse>> list(
            @RequestParam(required = false) String origin,
            @RequestParam(required = false) String destination,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        List<TravelPostResponse> out = travelService.search(
                Optional.ofNullable(origin),
                Optional.ofNullable(destination),
                Optional.ofNullable(date)
        );
        return ResponseEntity.ok(out);
    }

    @GetMapping("/travel/{id}")
    public ResponseEntity<TravelPostResponse> getOne(@PathVariable Long id) {
        return ResponseEntity.ok(travelService.getOne(id));
    }

    @DeleteMapping("/travel/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        travelService.delete(id);
        return ResponseEntity.ok().build();
    }
}
