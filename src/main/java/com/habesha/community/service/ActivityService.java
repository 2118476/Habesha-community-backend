package com.habesha.community.service;

import com.habesha.community.dto.ActivityItemDto;
import com.habesha.community.model.*;
import com.habesha.community.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Service responsible for assembling a recent activity feed for the
 * authenticated user. Activities include creations of events,
 * services, rentals and travel posts by the user, accepted friend
 * requests, incoming messages, and inbound interactions on the user's ads
 * (likes & comments). Results are ordered by descending timestamp and
 * filtered by an optional cutoff.
 */
@Service
@RequiredArgsConstructor
public class ActivityService {

    private final UserService userService;

    private final EventRepository eventRepository;
    private final ServiceOfferRepository serviceOfferRepository;
    private final RentalRepository rentalRepository;
    private final TravelPostRepository travelPostRepository;
    private final FriendRequestRepository friendRequestRepository;
    private final MessageRepository messageRepository;

    // NEW: inbound interactions on my ads
    private final AdLikeRepository adLikeRepository;
    private final AdCommentRepository adCommentRepository;

    /**
     * Returns a list of recent activity items for the current user.
     *
     * @param limit  maximum number of items to return
     * @param before only include items strictly before this timestamp (optional)
     */
    public List<ActivityItemDto> getRecentActivity(int limit, Instant before) {
        Optional<User> maybeUser = userService.getCurrentUser();
        if (maybeUser.isEmpty()) {
            return List.of();
        }
        User me = maybeUser.get();
        Long myId = me.getId();
        List<ActivityItemDto> items = new ArrayList<>();

        // Events created by me
        try {
            List<Event> myEvents = eventRepository.findAll(); // if you have findByOrganizer_Id(myId), prefer that
            for (Event e : myEvents) {
                if (e.getOrganizer() != null && e.getOrganizer().getId().equals(myId)) {
                    ActivityItemDto dto = new ActivityItemDto();
                    dto.setId("evt_" + e.getId());
                    dto.setType("EVENT_CREATED");
                    dto.setActor(userService.toSummary(me));
                    dto.setEntityType("event");
                    dto.setEntityId(e.getId());
                    dto.setTitle(e.getTitle());
                    LocalDateTime created = e.getCreatedAt();
                    dto.setCreatedAt(created != null ? created.atZone(ZoneId.systemDefault()).toInstant() : null);
                    items.add(dto);
                }
            }
        } catch (Exception ignored) {}

        // Services created by me
        try {
            List<ServiceOffer> offers = serviceOfferRepository.findByProvider_Id(myId);
            for (ServiceOffer o : offers) {
                ActivityItemDto dto = new ActivityItemDto();
                dto.setId("svc_" + o.getId());
                dto.setType("SERVICE_CREATED");
                dto.setActor(userService.toSummary(me));
                dto.setEntityType("service");
                dto.setEntityId(o.getId());
                dto.setTitle(o.getTitle());
                LocalDateTime created = o.getCreatedAt();
                dto.setCreatedAt(created != null ? created.atZone(ZoneId.systemDefault()).toInstant() : null);
                items.add(dto);
            }
        } catch (Exception ignored) {}

        // Rentals created by me
        try {
            List<Rental> rentals = rentalRepository.findByOwner_Id(myId);
            for (Rental r : rentals) {
                ActivityItemDto dto = new ActivityItemDto();
                dto.setId("rent_" + r.getId());
                dto.setType("RENTAL_CREATED");
                dto.setActor(userService.toSummary(me));
                dto.setEntityType("rental");
                dto.setEntityId(r.getId());
                dto.setTitle(r.getTitle());
                LocalDateTime created = r.getCreatedAt();
                dto.setCreatedAt(created != null ? created.atZone(ZoneId.systemDefault()).toInstant() : null);
                items.add(dto);
            }
        } catch (Exception ignored) {}

        // Travel posts created by me
        try {
            List<TravelPost> travels = travelPostRepository.findAll(); // if you have findByUser_Id(myId), prefer that
            for (TravelPost t : travels) {
                if (t.getUser() != null && t.getUser().getId().equals(myId)) {
                    ActivityItemDto dto = new ActivityItemDto();
                    dto.setId("trav_" + t.getId());
                    dto.setType("TRAVEL_POSTED");
                    dto.setActor(userService.toSummary(me));
                    dto.setEntityType("travel");
                    dto.setEntityId(t.getId());
                    String title = (t.getOriginCity() != null ? t.getOriginCity() : "From")
                            + " → "
                            + (t.getDestinationCity() != null ? t.getDestinationCity() : "To");
                    dto.setTitle(title);
                    LocalDateTime created = t.getCreatedAt();
                    dto.setCreatedAt(created != null ? created.atZone(ZoneId.systemDefault()).toInstant() : null);
                    items.add(dto);
                }
            }
        } catch (Exception ignored) {}

        // Friend acceptances involving me
        try {
            List<FriendRequest> frs = friendRequestRepository
                    .findBySenderOrReceiverAndStatus(me, me, FriendRequestStatus.ACCEPTED);
            for (FriendRequest fr : frs) {
                User other = fr.getSender().getId().equals(myId) ? fr.getReceiver() : fr.getSender();
                ActivityItemDto dto = new ActivityItemDto();
                dto.setId("friend_" + fr.getId());
                dto.setType("FRIEND_ACCEPTED");
                dto.setActor(userService.toSummary(other));
                dto.setEntityType("friend");
                dto.setEntityId(fr.getId());
                dto.setTitle("Friendship with " + (other != null ? other.getUsername() : "someone"));
                LocalDateTime created = fr.getCreatedAt();
                dto.setCreatedAt(created != null ? created.atZone(ZoneId.systemDefault()).toInstant() : null);
                items.add(dto);
            }
        } catch (Exception ignored) {}

        // Messages received by me (show recent inbound)
        try {
            List<Message> msgs = messageRepository.findRecentMessagesForUser(myId);
            for (Message m : msgs) {
                if (m.getRecipient() != null && m.getRecipient().getId().equals(myId)) {
                    ActivityItemDto dto = new ActivityItemDto();
                    dto.setId("msg_" + m.getId());
                    dto.setType("MESSAGE_RECEIVED");
                    dto.setActor(userService.toSummary(m.getSender()));
                    dto.setEntityType("message");
                    dto.setEntityId(m.getId());
                    String text = m.getContent();
                    if (text != null && text.length() > 50) {
                        text = text.substring(0, 47) + "…";
                    }
                    dto.setTitle(text != null ? text : "New message");
                    LocalDateTime sent = m.getSentAt();
                    dto.setCreatedAt(sent != null ? sent.atZone(ZoneId.systemDefault()).toInstant() : null);
                    items.add(dto);
                }
            }
        } catch (Exception ignored) {}

        // =====================================================================
        // NEW: inbound interactions on *my* ads (likes & comments)
        // =====================================================================

        // Likes on my ads
        try {
            List<AdLike> likes = adLikeRepository
                    .findTop50ByAd_Poster_IdOrderByCreatedAtDesc(myId);

            for (AdLike like : likes) {
                if (like == null || like.getAd() == null) continue;

                ActivityItemDto dto = new ActivityItemDto();
                dto.setId("adlike_" + like.getId());
                dto.setType("AD_LIKED");
                // actor: the user who liked
                User liker = like.getUser(); // adjust to like.getLiker() if your model uses that name
                dto.setActor(liker != null ? userService.toSummary(liker) : null);

                dto.setEntityType("ad");
                dto.setEntityId(like.getAd().getId());

                String adTitle = like.getAd().getTitle();
                dto.setTitle(adTitle != null ? "liked your ad: " + adTitle : "liked your ad");

                LocalDateTime created = like.getCreatedAt();
                dto.setCreatedAt(created != null
                        ? created.atZone(ZoneId.systemDefault()).toInstant()
                        : null);

                items.add(dto);
            }
        } catch (Exception ignored) {}

        // Comments on my ads
        try {
            List<AdComment> comments = adCommentRepository
                    .findTop50ByAd_Poster_IdOrderByCreatedAtDesc(myId);

            for (AdComment c : comments) {
                if (c == null || c.getAd() == null) continue;

                ActivityItemDto dto = new ActivityItemDto();
                dto.setId("adcomment_" + c.getId());
                dto.setType("AD_COMMENTED");

                // actor: the user who commented
                User author = c.getAuthor(); // adjust to c.getUser() if your model uses that name
                dto.setActor(author != null ? userService.toSummary(author) : null);

                dto.setEntityType("ad");
                dto.setEntityId(c.getAd().getId());

                String preview = c.getText();
                if (preview != null && preview.length() > 70) {
                    preview = preview.substring(0, 67) + "…";
                }
                String adTitle = c.getAd().getTitle();
                dto.setTitle(preview != null && !preview.isBlank()
                        ? preview
                        : (adTitle != null ? "commented on your ad: " + adTitle : "commented on your ad"));

                LocalDateTime created = c.getCreatedAt();
                dto.setCreatedAt(created != null
                        ? created.atZone(ZoneId.systemDefault()).toInstant()
                        : null);

                items.add(dto);
            }
        } catch (Exception ignored) {}

        // ---------------------------------------------------------------------

        // Apply 'before' filter
        if (before != null) {
            items.removeIf(i -> i.getCreatedAt() != null && !i.getCreatedAt().isBefore(before));
        }

        // Sort descending by createdAt (nulls last)
        items.sort(Comparator
                .comparing(ActivityItemDto::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder()))
                .reversed());

        // Trim to limit
        if (items.size() > limit) {
            return new ArrayList<>(items.subList(0, limit));
        }
        return items;
    }
}
