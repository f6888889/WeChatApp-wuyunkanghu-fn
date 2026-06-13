package com.muxin.model;

import java.util.List;

public class Course {
    private String id;
    private String title;
    private String description;
    private String difficulty;
    private String coverImage;
    private String videoUrl;
    private List<String> suitableFor;
    private List<String> benefits;
    private List<CourseStep> steps;
    private String createdAt;
    private String category;
    private int likeCount;

    public static class CourseStep {
        private String title;
        private int duration;
        private String description;

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public int getDuration() { return duration; }
        public void setDuration(int duration) { this.duration = duration; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }
    public String getCoverImage() { return coverImage; }
    public void setCoverImage(String coverImage) { this.coverImage = coverImage; }
    public String getVideoUrl() { return videoUrl; }
    public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }
    public List<String> getSuitableFor() { return suitableFor; }
    public void setSuitableFor(List<String> suitableFor) { this.suitableFor = suitableFor; }
    public List<String> getBenefits() { return benefits; }
    public void setBenefits(List<String> benefits) { this.benefits = benefits; }
    public List<CourseStep> getSteps() { return steps; }
    public void setSteps(List<CourseStep> steps) { this.steps = steps; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public int getLikeCount() { return likeCount; }
    public void setLikeCount(int likeCount) { this.likeCount = likeCount; }
}