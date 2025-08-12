package com.habesha.community.service;

import com.habesha.community.dto.MessageRequest;
import com.habesha.community.model.FriendRequestStatus;
import com.habesha.community.model.Message;
import com.habesha.community.model.User;
import com.habesha.community.repository.FriendRequestRepository;
import com.habesha.community.repository.MessageRepository;
import com.habesha.community.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final FriendRequestRepository friendRequestRepository;
    private final TwilioService twilioService;

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new IllegalStateException("No current user"));
    }

    @Transactional
    public void sendMessage(MessageRequest request) {
        User sender = getCurrentUser();
        User recipient = userRepository.findById(request.getRecipientId())
                .orElseThrow(() -> new IllegalArgumentException("Recipient not found"));

        // Ensure they are friends
        boolean friends = friendRequestRepository
                .findBySenderAndReceiver(sender, recipient)
                .map(fr -> fr.getStatus() == FriendRequestStatus.ACCEPTED)
                .orElse(false)
                || friendRequestRepository
                        .findBySenderAndReceiver(recipient, sender)
                        .map(fr -> fr.getStatus() == FriendRequestStatus.ACCEPTED)
                        .orElse(false);

        if (!friends) {
            throw new IllegalStateException("Users are not friends");
        }

        Message message = Message.builder()
                .sender(sender)
                .recipient(recipient)
                .content(request.getContent())
                .viaSms(request.isViaSms())
                .build();
        messageRepository.save(message);

        // Optionally send SMS
        if (request.isViaSms() && recipient.getPhone() != null && !recipient.getPhone().isEmpty()) {
            twilioService.sendSms(recipient.getPhone(), request.getContent());
        }
    }

    public List<Message> getConversation(Long otherUserId) {
        User current = getCurrentUser();
        User other = userRepository.findById(otherUserId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return messageRepository.findBySenderAndRecipientOrRecipientAndSenderOrderBySentAtAsc(
                current, other, current, other);
    }

    public long unreadCountForCurrentUser() {
        User me = getCurrentUser();
        return messageRepository.countByRecipient_IdAndReadByRecipientFalse(me.getId());
    }

    public List<MessageRepository.UnreadCountView> unreadSummaryForCurrentUser() {
        User me = getCurrentUser();
        return messageRepository.findUnreadCountsByRecipient(me.getId());
    }

    @Transactional
    public void markReadFromOther(Long otherUserId) {
        User me = getCurrentUser();
        messageRepository.markReadFromTo(otherUserId, me.getId());
    }
}
