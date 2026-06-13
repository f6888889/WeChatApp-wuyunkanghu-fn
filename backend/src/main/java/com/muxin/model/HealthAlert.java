package com.muxin.model;

import java.util.List;

public class HealthAlert {
    private String id;
    private String userId;
    private String userName;
    private String alertType;
    private String alertLevel;
    private String title;
    private String content;
    private boolean notified;
    private boolean read;
    private String createdAt;
    private List<HealthDataSnapshot> dataSnapshot;

    public static class HealthDataSnapshot {
        private String metricName;
        private String currentValue;
        private String expectedRange;
        private String deviation;

        public String getMetricName() { return metricName; }
        public void setMetricName(String metricName) { this.metricName = metricName; }
        public String getCurrentValue() { return currentValue; }
        public void setCurrentValue(String currentValue) { this.currentValue = currentValue; }
        public String getExpectedRange() { return expectedRange; }
        public void setExpectedRange(String expectedRange) { this.expectedRange = expectedRange; }
        public String getDeviation() { return deviation; }
        public void setDeviation(String deviation) { this.deviation = deviation; }
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public String getAlertType() { return alertType; }
    public void setAlertType(String alertType) { this.alertType = alertType; }
    public String getAlertLevel() { return alertLevel; }
    public void setAlertLevel(String alertLevel) { this.alertLevel = alertLevel; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public boolean isNotified() { return notified; }
    public void setNotified(boolean notified) { this.notified = notified; }
    public boolean isRead() { return read; }
    public void setRead(boolean read) { this.read = read; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public List<HealthDataSnapshot> getDataSnapshot() { return dataSnapshot; }
    public void setDataSnapshot(List<HealthDataSnapshot> dataSnapshot) { this.dataSnapshot = dataSnapshot; }
}