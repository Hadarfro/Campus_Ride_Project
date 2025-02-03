package com.example.campusride.models;

import java.util.Date;

public class Rating {
    private String ratingId;
    private String ratedUserId;
    private String ratingUserId;
    private double score;
    private String comment;
    private RatingType type;
    private Date timestamp;

    public Rating(String ratingId, String ratedUserId, String ratingUserId, double score,
                  String comment, RatingType type, Date timestamp) {
        this.ratingId = ratingId;
        this.ratedUserId = ratedUserId;
        this.ratingUserId = ratingUserId;
        this.score = score;
        this.comment = comment;
        this.type = type;
        this.timestamp = timestamp != null ? timestamp : new Date();
    }

    // Getters & Setters
    public String getRatingId() { return ratingId; }
    public String getRatedUserId() { return ratedUserId; }
    public String getRatingUserId() { return ratingUserId; }
    public double getScore() { return score; }
    public String getComment() { return comment; }
    public RatingType getType() { return type; }
    public Date getTimestamp() { return timestamp; }

    @Override
    public String toString() {
        return "Rating { " +
                "ID='" + ratingId + '\'' +
                ", Rated User='" + ratedUserId + '\'' +
                ", Rated By='" + ratingUserId + '\'' +
                ", Score=" + score +
                ", Comment='" + comment + '\'' +
                ", Type='" + type + '\'' +
                ", Timestamp=" + timestamp +
                '}';
    }
}
