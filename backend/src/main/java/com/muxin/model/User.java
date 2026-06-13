package com.muxin.model;

import java.util.List;

public class User {
    private String id;
    private String openId;
    private String nickname;
    private String gender;
    private int age;
    private String phone;
    private HealthProfile healthProfile;
    private LearningProgress learningProgress;
    private List<String> favorites;
    private List<String> recentSearches;
    private List<String> friends;
    private List<String> redeemedItems;
    private String createdAt;
    private int points;
    private int checkInContinuousDays;
    private String lastCheckInDate;
    private String lastRedeemDate;
    private EmergencyContact emergencyContact;

    public static class LearningHistory {
        private String courseId;
        private String courseTitle;
        private String coverImage;
        private int watchDuration;
        private int totalDuration;
        private String watchedAt;

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

    public static class HealthProfile {
        private boolean hasHypertension;
        private boolean hasDiabetes;
        private boolean hasArthritis;
        private boolean hasHeartDisease;
        private String mobilityLevel;

        public boolean isHasHypertension() { return hasHypertension; }
        public void setHasHypertension(boolean hasHypertension) { this.hasHypertension = hasHypertension; }
        public boolean isHasDiabetes() { return hasDiabetes; }
        public void setHasDiabetes(boolean hasDiabetes) { this.hasDiabetes = hasDiabetes; }
        public boolean isHasArthritis() { return hasArthritis; }
        public void setHasArthritis(boolean hasArthritis) { this.hasArthritis = hasArthritis; }
        public boolean isHasHeartDisease() { return hasHeartDisease; }
        public void setHasHeartDisease(boolean hasHeartDisease) { this.hasHeartDisease = hasHeartDisease; }
        public String getMobilityLevel() { return mobilityLevel; }
        public void setMobilityLevel(String mobilityLevel) { this.mobilityLevel = mobilityLevel; }
    }

    public static class LearningProgress {
        private int totalCourses;
        private int completedCourses;
        private int totalMinutes;
        private int continuousDays;
        private int onlineMinutes;
        private int todayOnlineMinutes;
        private String lastOnlineDate;
        private int dailyRecommendedMinutes;
        private int todayEarnedPoints;
        private int todayVideoPoints;

        public int getTotalCourses() { return totalCourses; }
        public void setTotalCourses(int totalCourses) { this.totalCourses = totalCourses; }
        public int getCompletedCourses() { return completedCourses; }
        public void setCompletedCourses(int completedCourses) { this.completedCourses = completedCourses; }
        public int getTotalMinutes() { return totalMinutes; }
        public void setTotalMinutes(int totalMinutes) { this.totalMinutes = totalMinutes; }
        public int getContinuousDays() { return continuousDays; }
        public void setContinuousDays(int continuousDays) { this.continuousDays = continuousDays; }
        public int getOnlineMinutes() { return onlineMinutes; }
        public void setOnlineMinutes(int onlineMinutes) { this.onlineMinutes = onlineMinutes; }
        public int getTodayOnlineMinutes() { return todayOnlineMinutes; }
        public void setTodayOnlineMinutes(int todayOnlineMinutes) { this.todayOnlineMinutes = todayOnlineMinutes; }
        public String getLastOnlineDate() { return lastOnlineDate; }
        public void setLastOnlineDate(String lastOnlineDate) { this.lastOnlineDate = lastOnlineDate; }
        public int getDailyRecommendedMinutes() { return dailyRecommendedMinutes; }
        public void setDailyRecommendedMinutes(int dailyRecommendedMinutes) { this.dailyRecommendedMinutes = dailyRecommendedMinutes; }
        public int getTodayEarnedPoints() { return todayEarnedPoints; }
        public void setTodayEarnedPoints(int todayEarnedPoints) { this.todayEarnedPoints = todayEarnedPoints; }
        public int getTodayVideoPoints() { return todayVideoPoints; }
        public void setTodayVideoPoints(int todayVideoPoints) { this.todayVideoPoints = todayVideoPoints; }
    }

    public static class EmergencyContact {
        private String name;
        private String phone;
        private String relationship;
        private boolean enabled;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        public String getRelationship() { return relationship; }
        public void setRelationship(String relationship) { this.relationship = relationship; }
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getOpenId() { return openId; }
    public void setOpenId(String openId) { this.openId = openId; }
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public HealthProfile getHealthProfile() { return healthProfile; }
    public void setHealthProfile(HealthProfile healthProfile) { this.healthProfile = healthProfile; }
    public LearningProgress getLearningProgress() { return learningProgress; }
    public void setLearningProgress(LearningProgress learningProgress) { this.learningProgress = learningProgress; }
    public List<String> getFavorites() { return favorites; }
    public void setFavorites(List<String> favorites) { this.favorites = favorites; }
    public List<String> getRecentSearches() { return recentSearches; }
    public void setRecentSearches(List<String> recentSearches) { this.recentSearches = recentSearches; }
    public List<String> getFriends() { return friends; }
    public void setFriends(List<String> friends) { this.friends = friends; }
    public List<String> getRedeemedItems() { return redeemedItems; }
    public void setRedeemedItems(List<String> redeemedItems) { this.redeemedItems = redeemedItems; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public int getPoints() { return points; }
    public void setPoints(int points) { this.points = points; }

    public int getCheckInContinuousDays() { return checkInContinuousDays; }
    public void setCheckInContinuousDays(int checkInContinuousDays) { this.checkInContinuousDays = checkInContinuousDays; }

    public String getLastCheckInDate() { return lastCheckInDate; }
    public void setLastCheckInDate(String lastCheckInDate) { this.lastCheckInDate = lastCheckInDate; }

    public String getLastRedeemDate() { return lastRedeemDate; }
    public void setLastRedeemDate(String lastRedeemDate) { this.lastRedeemDate = lastRedeemDate; }

    public EmergencyContact getEmergencyContact() { return emergencyContact; }
    public void setEmergencyContact(EmergencyContact emergencyContact) { this.emergencyContact = emergencyContact; }
}