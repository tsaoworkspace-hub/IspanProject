package com.project.sneakermarket.calendar.controller;

import com.project.sneakermarket.calendar.dto.CalendarEventDTO;
import com.project.sneakermarket.calendar.entity.CalendarEvent;
import com.project.sneakermarket.calendar.repository.CalendarEventRepository;
import com.project.sneakermarket.calendar.service.CalendarEventService;
import com.project.sneakermarket.common.security.SessionAuthorizationService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Optional;

import jakarta.validation.Valid;


@RestController
public class CalendarEventController {

    @Autowired
    private CalendarEventService calendarEventService;

    @Autowired
    private CalendarEventRepository calendarEventRepo;

    @Autowired
    private SessionAuthorizationService authorizationService;



    @PostMapping("/api/calendar/eventCreate")
    public ResponseEntity<CalendarEvent> createEvent(
            @Valid
            @RequestBody
            CalendarEventDTO request,
            HttpServletRequest httpRequest
    ) {
        authorizationService.requireAdmin(httpRequest);
        CalendarEvent created =
                calendarEventService.createEvent(
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(created);
    }


    @PutMapping("/api/calendar/eventUpdate/{id}")
    public ResponseEntity<?> updateEvent(
            @PathVariable("id")
            Long eventId,

            @Valid
            @RequestBody
            CalendarEventDTO request,
            HttpServletRequest httpRequest
    ) {
        authorizationService.requireAdmin(httpRequest);
        calendarEventService.updateEventById(
                eventId,
                request
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .build();
    }



    @DeleteMapping("/api/calendar/eventDelete")
    public ResponseEntity<?> deleteEvent(
            @RequestParam Long eventId,
            HttpServletRequest httpRequest){
        authorizationService.requireAdmin(httpRequest);
        calendarEventService.cancelEventById(eventId);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @GetMapping("/api/calendar/eventPage")
    public ResponseEntity<Page<CalendarEvent>> getEvent(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String eventType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)LocalDateTime startAt,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)LocalDateTime endAt,
            @RequestParam(defaultValue = "false") boolean overlap,
            @RequestParam(defaultValue = "1") Integer pageNumber,
            @RequestParam(defaultValue = "10") Integer pageSize) {

        Page<CalendarEvent> events = calendarEventService.searchEvent(
                title, eventType, status, startAt, endAt, overlap, pageNumber, pageSize);

        return ResponseEntity.ok(events);
    }

    @GetMapping("/api/calendar/event/{id}/photo")
    public ResponseEntity<byte[]> getEventPhoto(@PathVariable Long id) {
        Optional<CalendarEvent> op = calendarEventRepo.findById(id); // 或透過 service 找
        if (op.isPresent() && op.get().getPhoto() != null) {
            return ResponseEntity.ok()
                    .contentType(org.springframework.http.MediaType.IMAGE_JPEG) // 或根據圖片型態設定
                    .body(op.get().getPhoto());
        }
        return ResponseEntity.notFound().build();
    }


}
