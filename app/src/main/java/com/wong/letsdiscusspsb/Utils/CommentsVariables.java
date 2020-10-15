package com.wong.letsdiscusspsb.Utils;

public class CommentsVariables {

    private String username;
    private String profileImageUrl;
    private String comment;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public CommentsVariables() {
    }

    public CommentsVariables(String username, String profileImageUrl, String comment, String date) {
        this.username = username;
        this.profileImageUrl = profileImageUrl;
        this.comment = comment;
        this.date = date;
    }

    private String date;

}
