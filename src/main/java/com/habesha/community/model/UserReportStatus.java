package com.habesha.community.model;

/**
 * Status of a user report.
 * OPEN      - freshly submitted, needs review
 * REVIEWED  - a moderator/admin looked at it
 * CLOSED    - handled / resolved / dismissed
 */
public enum UserReportStatus {
    OPEN,
    REVIEWED,
    CLOSED
}
