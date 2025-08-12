package com.habesha.community.repository;

import com.habesha.community.model.Message;
import com.habesha.community.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    /**
     * Returns all messages exchanged between two users, ordered by send time ascending.
     */
    List<Message> findBySenderAndRecipientOrRecipientAndSenderOrderBySentAtAsc(
            User sender1, User recipient1, User sender2, User recipient2
    );

    /**
     * Total unread messages for a given recipient.
     */
    long countByRecipient_IdAndReadByRecipientFalse(Long recipientId);

    /**
     * Mark all messages from sender -> recipient as read.
     */
    @Transactional
    @Modifying
    @Query("""
            update Message m
               set m.readByRecipient = true,
                   m.readAt = CURRENT_TIMESTAMP
             where m.sender.id = :senderId
               and m.recipient.id = :recipientId
               and m.readByRecipient = false
            """)
    int markReadFromTo(Long senderId, Long recipientId);

    /**
     * Projection for per-friend unread counts.
     */
    interface UnreadCountView {
        Long getUserId();
        Long getCount();
    }

    /**
     * Find unread counts grouped by sender for the given recipient.
     */
    @Query("""
        select m.sender.id as userId, count(m) as count
          from Message m
         where m.recipient.id = :recipientId
           and m.readByRecipient = false
         group by m.sender.id
    """)
    List<UnreadCountView> findUnreadCountsByRecipient(Long recipientId);
}
