package com.habesha.community.service;

import com.habesha.community.model.AccountDeletionRequest;
import com.habesha.community.model.AccountDeletionRequest.AccountDeletionStatus;
import com.habesha.community.model.Role;
import com.habesha.community.model.User;
import com.habesha.community.repository.AccountDeletionRequestRepository;
import com.habesha.community.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AccountDeletionService {

    private final AccountDeletionRequestRepository deletionRequestRepository;
    private final UserRepository userRepository;
    private final MailService mailService;

    /**
     * Create a new account deletion request
     */
    public AccountDeletionRequest createDeletionRequest(User user) {
        // Check if user already has a pending request
        var existingRequest = deletionRequestRepository.findTopByUser_IdOrderByCreatedAtDesc(user.getId());
        if (existingRequest.isPresent() && existingRequest.get().getStatus() == AccountDeletionStatus.PENDING) {
            return existingRequest.get();
        }

        // Create new request
        AccountDeletionRequest request = AccountDeletionRequest.builder()
                .user(user)
                .status(AccountDeletionStatus.PENDING)
                .build();

        request = deletionRequestRepository.save(request);

        // Notify admins and moderators
        notifyAdminsAndModerators(request);

        return request;
    }

    private void notifyAdminsAndModerators(AccountDeletionRequest request) {
        try {
            // Get all admins and moderators
            List<User> admins = userRepository.findByRole(Role.ADMIN);
            List<User> moderators = userRepository.findByRole(Role.MODERATOR);

            String subject = "Account Deletion Request";
            String body = String.format(
                "A user has requested account deletion.\n\n" +
                "User: %s (%s)\n" +
                "Request ID: %d\n" +
                "Request Time: %s\n\n" +
                "Please review this request in the admin panel.",
                request.getUser().getName(),
                request.getUser().getEmail(),
                request.getId(),
                request.getCreatedAt()
            );

            // Send emails to admins
            for (User admin : admins) {
                if (admin.getEmail() != null) {
                    mailService.sendEmail(admin.getEmail(), subject, body);
                }
            }

            // Send emails to moderators
            for (User moderator : moderators) {
                if (moderator.getEmail() != null) {
                    mailService.sendEmail(moderator.getEmail(), subject, body);
                }
            }
        } catch (Exception e) {
            // Log error but don't fail the request creation
            System.err.println("Failed to notify admins/moderators about deletion request: " + e.getMessage());
        }
    }
}