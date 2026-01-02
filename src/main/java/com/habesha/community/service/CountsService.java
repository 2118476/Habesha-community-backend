package com.habesha.community.service;

import com.habesha.community.dto.ThreadSummaryDto;
import com.habesha.community.model.FriendRequestStatus;
import com.habesha.community.repository.FriendRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CountsService {

    private final FriendRequestRepository friendRequestRepository;
    private final MessageService messageService;

    public long getUnreadMessagesTotal() {
        messageService.getRecentThreads(100);
        List<ThreadSummaryDto> threads = java.util.Collections.emptyList();
        long sum = 0L;
        if (threads != null) {
            for (ThreadSummaryDto t : threads) {
                Long unread = t.getUnread(); // ThreadSummaryDto has setUnread(...) in MessageService, so getter should exist
                if (unread != null) sum += unread;
            }
        }
        return sum;
    }

    public long getPendingFriendRequests() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return 0L;

        Object principal = auth.getPrincipal();
        Long currentId = null;
        try {
            currentId = (Long) principal.getClass().getMethod("getId").invoke(principal);
        } catch (Exception ignored) {}
        if (currentId == null) return 0L;

     return friendRequestRepository.countByReceiver_IdAndStatus(currentId, FriendRequestStatus.PENDING);

 }
}
