package com.muxin.model;

public class LearningRecord {
    private String id;
    private String userId;
    private String courseId;
    private String courseTitle;
    private String coverImage;
    private int watchDuration;
    private int totalDuration;
    private String watchedAt;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getCourseId() { return courseId; }
    public void setCourseId(String courseId) { this.courseId = courseId; }
    public String getCourseTitle() { return courseTitle; }
    public void setCourseTitle(String courseTitle) { this.courseTitle = courseTitle; }
    public String getCoverImage() { return coverImage; }
    public void setCoverImage(String coverImage) { this.coverImage = coverImage; }
    public int getWatchDuration() { return watchDuration; }
    public void setWatchDuration(int watchDuration) { this.watchDuration = watchDuration; }
    public int getTotalDuration() { return totalDuration; }
    public void setTotalDuration(int totalDuration) { this.totalDuration = totalDuration; }
    public String getWatchedAt() { return watchedAt; }
    public void setWatchedAt(String watchedAt) { this.watchedAt = watchedAt; }
}
