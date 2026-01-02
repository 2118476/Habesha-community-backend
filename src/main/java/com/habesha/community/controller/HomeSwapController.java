package com.habesha.community.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.habesha.community.dto.HomeSwapRequest;
import com.habesha.community.dto.HomeSwapResponse;
import com.habesha.community.service.HomeSwapService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping // keep root; adjust if you have a global "/api"
@RequiredArgsConstructor
public class HomeSwapController {

    private final HomeSwapService service;
    private final ObjectMapper objectMapper;

    /**
     * Create with multipart: JSON + photos[]
     * FE:
     *   fd.append("data", JSON.stringify(form));             // as string (default)
     *   fd.append("photos", file);                           // repeat for each
     */
    @PostMapping(
            value = "/homeswap",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<HomeSwapResponse> createMultipart(
            @RequestPart("data") String dataJson,
            @RequestPart(value = "photos", required = false) List<MultipartFile> photos
    ) throws Exception {
        if (dataJson == null || dataJson.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        HomeSwapRequest data = objectMapper.readValue(dataJson, HomeSwapRequest.class);
        HomeSwapResponse created = service.create(data, photos);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /** Optional JSON-only create (no photos). */
    @PostMapping(
            value = "/homeswap",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<HomeSwapResponse> createJson(@RequestBody HomeSwapRequest data) {
        HomeSwapResponse created = service.create(data);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping(value = "/homeswap", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<HomeSwapResponse>> list() {
        return ResponseEntity.ok(service.list());
    }

    @GetMapping(value = "/homeswap/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<HomeSwapResponse> detail(@PathVariable Long id) {
        return ResponseEntity.ok(service.getOne(id));
    }

    @PutMapping(value = "/homeswap/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<HomeSwapResponse> update(@PathVariable Long id, @RequestBody HomeSwapRequest req) {
        return ResponseEntity.ok(service.update(id, req));
    }

    @DeleteMapping("/homeswap/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
