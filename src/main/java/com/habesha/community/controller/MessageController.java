package com.habesha.community.controller;

import com.habesha.community.dto.MessageRequest;
import com.habesha.community.dto.SmsRequest;
import com.habesha.community.model.Message;
import com.habesha.community.repository.MessageRepository;
import com.habesha.community.service.MessageService;
import com.habesha.community.service.TwilioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;
    private final TwilioService twilioService;

    @PostMapping("/messages/send")
    public ResponseEntity<Void> sendMessage(@Valid @RequestBody MessageRequest request) {
        messageService.sendMessage(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/messages/{userId}")
    public ResponseEntity<List<Message>> getConversation(@PathVariable Long userId) {
        return ResponseEntity.ok(messageService.getConversation(userId));
    }

    @PostMapping("/sms/send")
    public ResponseEntity<Void> sendSms(@Valid @RequestBody SmsRequest request) {
        twilioService.sendSms(request.getToNumber(), request.getMessage());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/messages/unread-count")
    public ResponseEntity<Long> unreadCount() {
        return ResponseEntity.ok(messageService.unreadCountForCurrentUser());
    }

    @GetMapping("/messages/unread-summary")
    public ResponseEntity<List<MessageRepository.UnreadCountView>> unreadSummary() {
        return ResponseEntity.ok(messageService.unreadSummaryForCurrentUser());
    }

    @PostMapping("/messages/read/{otherUserId}")
    public ResponseEntity<Void> markRead(@PathVariable Long otherUserId) {
        messageService.markReadFromOther(otherUserId);
        return ResponseEntity.ok().build();
    }
}
