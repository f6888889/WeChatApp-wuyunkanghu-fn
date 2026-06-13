package com.muxin.model;

import java.util.List;

public class HealthReport {
    private String userId;
    private PhysicalAssessment physicalAssessment;
    private EmotionalState emotionalState;
    private ExerciseLevel exerciseLevel;
    private List<RehabilitationTag> rehabilitationTags;
    private List<CourseRecommendation> courseRecommendations;
    private String generatedAt;

    public static class PhysicalAssessment {
        private int jointFlexibilityScore;
        private int balanceScore;
        private int staminaScore;
        private String overallLevel;
        private String recommendedDanceType;

        public int getJointFlexibilityScore() { return jointFlexibilityScore; }
        public void setJointFlexibilityScore(int jointFlexibilityScore) { this.jointFlexibilityScore = jointFlexibilityScore; }
        public int getBalanceScore() { return balanceScore; }
        public void setBalanceScore(int balanceScore) { this.balanceScore = balanceScore; }
        public int getStaminaScore() { return staminaScore; }
        public void setStaminaScore(int staminaScore) { this.staminaScore = staminaScore; }
        public String getOverallLevel() { return overallLevel; }
        public void setOverallLevel(String overallLevel) { this.overallLevel = overallLevel; }
        public String getRecommendedDanceType() { return recommendedDanceType; }
        public void setRecommendedDanceType(String recommendedDanceType) { this.recommendedDanceType = recommendedDanceType; }
    }

    public static class EmotionalState {
        private int hrvScore;
        private int selfAssessmentScore;
        private String stressLevel;
        private String moodState;
        private String recommendedDanceType;

        public int getHrvScore() { return hrvScore; }
        public void setHrvScore(int hrvScore) { this.hrvScore = hrvScore; }
        public int getSelfAssessmentScore() { return selfAssessmentScore; }
        public void setSelfAssessmentScore(int selfAssessmentScore) { this.selfAssessmentScore = selfAssessmentScore; }
        public String getStressLevel() { return stressLevel; }
        public void setStressLevel(String stressLevel) { this.stressLevel = stressLevel; }
        public String getMoodState() { return moodState; }
        public void setMoodState(String moodState) { this.moodState = moodState; }
        public String getRecommendedDanceType() { return recommendedDanceType; }
        public void setRecommendedDanceType(String recommendedDanceType) { this.recommendedDanceType = recommendedDanceType; }
    }

    public static class ExerciseLevel {
        private String level;
        private int completionRate;
        private int totalMinutes;
        private int difficultyRecommendation;
        private List<String> suitableCourseIds;

        public String getLevel() { return level; }
        public void setLevel(String level) { this.level = level; }
        public int getCompletionRate() { return completionRate; }
        public void setCompletionRate(int completionRate) { this.completionRate = completionRate; }
        public int getTotalMinutes() { return totalMinutes; }
        public void setTotalMinutes(int totalMinutes) { this.totalMinutes = totalMinutes; }
        public int getDifficultyRecommendation() { return difficultyRecommendation; }
        public void setDifficultyRecommendation(int difficultyRecommendation) { this.difficultyRecommendation = difficultyRecommendation; }
        public List<String> getSuitableCourseIds() { return suitableCourseIds; }
        public void setSuitableCourseIds(List<String> suitableCourseIds) { this.suitableCourseIds = suitableCourseIds; }
    }

    public static class RehabilitationTag {
        private String tag;
        private String description;
        private String severity;
        private List<String> recommendedCourseIds;

        public String getTag() { return tag; }
        public void setTag(String tag) { this.tag = tag; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getSeverity() { return severity; }
        public void setSeverity(String severity) { this.severity = severity; }
        public List<String> getRecommendedCourseIds() { return recommendedCourseIds; }
        public void setRecommendedCourseIds(List<String> recommendedCourseIds) { this.recommendedCourseIds = recommendedCourseIds; }
    }

    public static class CourseRecommendation {
        private String courseId;
        private String courseTitle;
        private String reason;
        private String matchLevel;
        private String category;

        public String getCourseId() { return courseId; }
        public void setCourseId(String courseId) { this.courseId = courseId; }
        public String getCourseTitle() { return courseTitle; }
        public void setCourseTitle(String courseTitle) { this.courseTitle = courseTitle; }
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
        public String getMatchLevel() { return matchLevel; }
        public void setMatchLevel(String matchLevel) { this.matchLevel = matchLevel; }
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public PhysicalAssessment getPhysicalAssessment() { return physicalAssessment; }
    public void setPhysicalAssessment(PhysicalAssessment physicalAssessment) { this.physicalAssessment = physicalAssessment; }
    public EmotionalState getEmotionalState() { return emotionalState; }
    public void setEmotionalState(EmotionalState emotionalState) { this.emotionalState = emotionalState; }
    public ExerciseLevel getExerciseLevel() { return exerciseLevel; }
    public void setExerciseLevel(ExerciseLevel exerciseLevel) { this.exerciseLevel = exerciseLevel; }
    public List<RehabilitationTag> getRehabilitationTags() { return rehabilitationTags; }
    public void setRehabilitationTags(List<RehabilitationTag> rehabilitationTags) { this.rehabilitationTags = rehabilitationTags; }
    public List<CourseRecommendation> getCourseRecommendations() { return courseRecommendations; }
    public void setCourseRecommendations(List<CourseRecommendation> courseRecommendations) { this.courseRecommendations = courseRecommendations; }
    public String getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(String generatedAt) { this.generatedAt = generatedAt; }
}