package com.habesha.community.service;

import com.habesha.community.model.AuditLog;
import com.habesha.community.model.User;
import com.habesha.community.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Records moderation/admin actions to the audit log. Best-effort: a logging
 * failure must never break the action it is recording.
 */
@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository repo;

    public void record(User actor, String action, String targetType, Long targetId, String detail) {
        try {
            String actorName = actor == null ? "system"
                    : (actor.getName() != null && !actor.getName().isBlank() ? actor.getName()
                       : (actor.getUsername() != null ? actor.getUsername() : actor.getEmail()));
            repo.save(AuditLog.builder()
                    .actorId(actor == null ? null : actor.getId())
                    .actorName(actorName)
                    .action(action)
                    .targetType(targetType)
                    .targetId(targetId)
                    .detail(detail == null ? null : (detail.length() > 2000 ? detail.substring(0, 2000) : detail))
                    .build());
        } catch (Exception ignore) {
            // never let auditing break the real action
        }
    }
}
