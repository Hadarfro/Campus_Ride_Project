package com.example.projectcampusride;


public class NotificationModel {
    private String id;
    private String message;
    private String senderId;
    private String recipientId;
    private String status; // "pending", "approved", "refused"

    public NotificationModel() {
        // Required for Firestore
    }

    public NotificationModel(String id, String message, String senderId, String recipientId, String status) {
        this.id = id;
        this.message = message;
        this.senderId = senderId;
        this.recipientId = recipientId;
        this.status = status;
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }
    public String getRecipientId() { return recipientId; }
    public void setRecipientId(String recipientId) { this.recipientId = recipientId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}

