// package com.habesha.community.controller;

// import com.habesha.community.dto.MessageRequest;
// import com.habesha.community.dto.SmsRequest;
// import com.habesha.community.dto.ThreadSummaryDto;
// import com.habesha.community.model.Message;
// import com.habesha.community.repository.MessageRepository;
// import com.habesha.community.service.MessageService;
// import com.habesha.community.service.TwilioService;
// import jakarta.validation.Valid;
// import lombok.RequiredArgsConstructor;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.*;

// import java.util.List;

// @RestController
// @RequestMapping
// @RequiredArgsConstructor
// public class MessageController {

//     private final MessageService messageService;
//     private final TwilioService twilioService;

//     /* ----------------------------- Messaging ------------------------------ */

//     @PostMapping("/messages/send")
//     public ResponseEntity<Void> sendMessage(@Valid @RequestBody MessageRequest request) {
//         messageService.sendMessage(request);
//         return ResponseEntity.ok().build();
//     }

//     @GetMapping("/messages/{userId}")
//     public ResponseEntity<List<Message>> getConversation(@PathVariable Long userId) {
//         return ResponseEntity.ok(messageService.getConversation(userId));
//     }

//     @PostMapping("/messages/read/{otherUserId}")
//     public ResponseEntity<Void> markRead(@PathVariable Long otherUserId) {
//         messageService.markReadFromOther(otherUserId);
//         return ResponseEntity.ok().build();
//     }

//     /* ------------------------------- SMS ---------------------------------- */

//     @PostMapping("/sms/send")
//     public ResponseEntity<Void> sendSms(@Valid @RequestBody SmsRequest request) {
//         twilioService.sendSms(request.getToNumber(), request.getMessage());
//         return ResponseEntity.ok().build();
//     }

//     /* ----------------------------- Unread API ----------------------------- */

//     @GetMapping("/messages/unread-count")
//     public ResponseEntity<Long> unreadCount() {
//         return ResponseEntity.ok(messageService.unreadCountForCurrentUser());
//     }

//     @GetMapping("/messages/unread-summary")
//     public ResponseEntity<List<MessageRepository.UnreadCountView>> unreadSummary() {
//         return ResponseEntity.ok(messageService.unreadSummaryForCurrentUser());
//     }

//     /* --------------------------- Threads (Inbox) --------------------------- */

//     /**
//      * Return a summary of the most recent message threads for the current user.
//      * Threads are ordered by the time of the last message (descending).
//      * Each summary includes the peer's id, name, avatarUrl, lastText, lastAt,
//      * and unread count—sufficient for the sidebar "recent messages" list.
//      *
//      * @param limit optional maximum number of threads to return (default 50)
//      */
//     @GetMapping("/api/messages/threads")
//     public ResponseEntity<List<ThreadSummaryDto>> getThreads(
//             @RequestParam(name = "limit", required = false) Integer limit) {

//         final int lim = (limit == null || limit < 1) ? 50 : limit;
//         return ResponseEntity.ok(messageService.getRecentThreads(lim));
//     }
// }
