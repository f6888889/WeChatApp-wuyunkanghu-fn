package com.muxin.model;

import java.util.List;

public class Podcast {
    private String id;
    private String title;
    private String description;
    private String coverImage;
    private String author;
    private String duration;
    private String audioUrl;
    private String category;
    private List<PodcastContent> content;

    public static class PodcastContent {
        private String type;
        private String value;

        public PodcastContent() {}

        public PodcastContent(String type, String value) {
            this.type = type;
            this.value = value;
        }

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getValue() { return value; }
        public void setValue(String value) { this.value = value; }
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getCoverImage() { return coverImage; }
    public void setCoverImage(String coverImage) { this.coverImage = coverImage; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    public String getDuration() { return duration; }
    public void setDuration(String duration) { this.duration = duration; }
    public String getAudioUrl() { return audioUrl; }
    public void setAudioUrl(String audioUrl) { this.audioUrl = audioUrl; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public List<PodcastContent> getContent() { return content; }
    public void setContent(List<PodcastContent> content) { this.content = content; }
}
