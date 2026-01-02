package com.habesha.community.dto;

/**
 * Fields that can be edited by the owner/admin from "Edit Rental".
 * We keep them nullable so the service can do partial updates.
 *
 * price comes in as string (e.g. "500") so we can parse.
 */
public class RentalUpdateRequest {
    private String title;
    private String description;
    private String price;
    private String location;
    private String roomType;
    private String contact;
    private Boolean featured; // checkbox

    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    public String getPrice() {
        return price;
    }
    public void setPrice(String price) {
        this.price = price;
    }

    public String getLocation() {
        return location;
    }
    public void setLocation(String location) {
        this.location = location;
    }

    public String getRoomType() {
        return roomType;
    }
    public void setRoomType(String roomType) {
        this.roomType = roomType;
    }

    public String getContact() {
        return contact;
    }
    public void setContact(String contact) {
        this.contact = contact;
    }

    public Boolean getFeatured() {
        return featured;
    }
    public void setFeatured(Boolean featured) {
        this.featured = featured;
    }
}
