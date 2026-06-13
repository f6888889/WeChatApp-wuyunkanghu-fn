package com.muxin.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.muxin.model.Course;
import com.muxin.model.HealthReport;
import com.muxin.model.HealthReport.*;
import com.muxin.model.HealthReminder;
import com.muxin.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class HealthService {

    @Autowired
    private JsonDataService jsonDataService;

    private static final String HEALTH_FILE = "health_reminders.json";
    private static final String COURSES_FILE = "courses.json";

    public List<HealthReminder> getAllReminders() {
        return jsonDataService.readJsonFile(HEALTH_FILE, new TypeReference<List<HealthReminder>>() {});
    }

    public List<HealthReminder> getDailyReminders() {
        List<HealthReminder> all = getAllReminders();
        String currentTimeSlot = getCurrentTimeSlot();
        return all.stream()
                .filter(r -> "reminder".equals(r.getType()))
                .filter(r -> r.getTimeSlot().equals(currentTimeSlot) || r.getTimeSlot().equals("all"))
                .collect(Collectors.toList());
    }

    public List<HealthReminder> getHealthTips() {
        List<HealthReminder> all = getAllReminders();
        return all.stream()
                .filter(r -> "tip".equals(r.getType()))
                .collect(Collectors.toList());
    }

    public List<HealthReminder> getRemindersByTimeSlot(String timeSlot) {
        List<HealthReminder> all = getAllReminders();
        return all.stream()
                .filter(r -> "reminder".equals(r.getType()))
                .filter(r -> r.getTimeSlot().equals(timeSlot))
                .collect(Collectors.toList());
    }

    private String getCurrentTimeSlot() {
        int hour = LocalTime.now().getHour();
        if (hour >= 6 && hour < 12) {
            return "morning";
        } else if (hour >= 12 && hour < 18) {
            return "afternoon";
        } else {
            return "evening";
        }
    }

    public HealthReport generateHealthReport(User user) {
        HealthReport report = new HealthReport();
        report.setUserId(user.getId());
        report.setGeneratedAt(java.time.LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        report.setPhysicalAssessment(analyzePhysicalAssessment(user));
        report.setEmotionalState(analyzeEmotionalState(user));
        report.setExerciseLevel(analyzeExerciseLevel(user));
        report.setRehabilitationTags(identifyRehabilitationNeeds(user));
        report.setCourseRecommendations(generateCourseRecommendations(user, report));

        return report;
    }

    private PhysicalAssessment analyzePhysicalAssessment(User user) {
        PhysicalAssessment assessment = new PhysicalAssessment();

        int jointScore = 50;
        int balanceScore = 50;
        int staminaScore = 50;

        User.HealthProfile healthProfile = user.getHealthProfile();
        if (healthProfile != null) {
            String mobility = healthProfile.getMobilityLevel();
            if ("excellent".equals(mobility)) {
                jointScore = 90;
                balanceScore = 85;
                staminaScore = 80;
            } else if ("good".equals(mobility)) {
                jointScore = 75;
                balanceScore = 70;
                staminaScore = 65;
            } else if ("some_limitation".equals(mobility)) {
                jointScore = 50;
                balanceScore = 45;
                staminaScore = 40;
            } else if ("limited".equals(mobility)) {
                jointScore = 30;
                balanceScore = 25;
                staminaScore = 20;
            }

            if (healthProfile.isHasArthritis()) {
                jointScore = Math.max(20, jointScore - 30);
                balanceScore = Math.max(20, balanceScore - 15);
            }
            if (healthProfile.isHasHeartDisease()) {
                staminaScore = Math.max(20, staminaScore - 25);
            }
            if (healthProfile.isHasHypertension()) {
                staminaScore = Math.max(20, staminaScore - 15);
            }
        }

        int avgScore = (jointScore + balanceScore + staminaScore) / 3;
        String overallLevel;
        String recommendedDanceType;

        if (avgScore >= 80) {
            overallLevel = "优秀";
            recommendedDanceType = "活力律动类舞蹈";
        } else if (avgScore >= 60) {
            overallLevel = "良好";
            recommendedDanceType = "舒缓拉伸类舞蹈";
        } else if (avgScore >= 40) {
            overallLevel = "一般";
            recommendedDanceType = "舒缓拉伸类舞蹈";
        } else {
            overallLevel = "需改善";
            recommendedDanceType = "舒缓拉伸类舞蹈";
        }

        assessment.setJointFlexibilityScore(jointScore);
        assessment.setBalanceScore(balanceScore);
        assessment.setStaminaScore(staminaScore);
        assessment.setOverallLevel(overallLevel);
        assessment.setRecommendedDanceType(recommendedDanceType);

        return assessment;
    }

    private EmotionalState analyzeEmotionalState(User user) {
        EmotionalState state = new EmotionalState();

        int hrvScore = 70;
        int selfAssessmentScore = 65;

        List<String> recentSearches = user.getRecentSearches();
        if (recentSearches != null && !recentSearches.isEmpty()) {
            List<String> stressKeywords = Arrays.asList("压力大", "焦虑", "失眠", "心情不好", "抑郁", "烦躁");
            List<String> lowMoodKeywords = Arrays.asList("无聊", "寂寞", "不开心", "低落", "疲惫");

            long stressCount = recentSearches.stream()
                    .filter(s -> stressKeywords.stream().anyMatch(k -> s.contains(k)))
                    .count();
            long lowMoodCount = recentSearches.stream()
                    .filter(s -> lowMoodKeywords.stream().anyMatch(k -> s.contains(k)))
                    .count();

            if (stressCount > 0) {
                hrvScore = Math.max(30, 70 - (int)(stressCount * 15));
                selfAssessmentScore = Math.max(40, selfAssessmentScore - (int)(stressCount * 10));
            }
            if (lowMoodCount > 0) {
                selfAssessmentScore = Math.max(35, selfAssessmentScore - (int)(lowMoodCount * 12));
            }
        }

        String stressLevel;
        String moodState;
        String recommendedDanceType;

        if (hrvScore >= 70 && selfAssessmentScore >= 65) {
            stressLevel = "轻松";
            moodState = "愉悦";
            recommendedDanceType = "活力律动类舞蹈";
        } else if (hrvScore >= 50 && selfAssessmentScore >= 50) {
            stressLevel = "适中";
            moodState = "平静";
            recommendedDanceType = "舒缓拉伸类舞蹈";
        } else if (hrvScore >= 30 && selfAssessmentScore >= 35) {
            stressLevel = "偏高";
            moodState = "有压力";
            recommendedDanceType = "放松疗愈型舞蹈";
        } else {
            stressLevel = "过高";
            moodState = "低落";
            recommendedDanceType = "欢快释放型舞蹈";
        }

        state.setHrvScore(hrvScore);
        state.setSelfAssessmentScore(selfAssessmentScore);
        state.setStressLevel(stressLevel);
        state.setMoodState(moodState);
        state.setRecommendedDanceType(recommendedDanceType);

        return state;
    }

    private ExerciseLevel analyzeExerciseLevel(User user) {
        ExerciseLevel level = new ExerciseLevel();

        User.LearningProgress progress = user.getLearningProgress();
        int totalMinutes = progress != null ? progress.getTotalMinutes() : 0;
        int completedCourses = progress != null ? progress.getCompletedCourses() : 0;
        int totalCourses = progress != null ? progress.getTotalCourses() : 0;

        int completionRate = totalCourses > 0 ? (completedCourses * 100 / totalCourses) : 0;

        String difficultyLevel;
        int difficultyRecommendation;

        if (totalMinutes >= 300 && completionRate >= 70) {
            difficultyLevel = "高级";
            difficultyRecommendation = 3;
        } else if (totalMinutes >= 120 && completionRate >= 50) {
            difficultyLevel = "中级";
            difficultyRecommendation = 2;
        } else if (totalMinutes >= 30) {
            difficultyLevel = "初级";
            difficultyRecommendation = 1;
        } else {
            difficultyLevel = "初学者";
            difficultyRecommendation = 1;
        }

        List<Course> allCourses = jsonDataService.readJsonFile(COURSES_FILE, new TypeReference<List<Course>>() {});
        List<String> suitableCourseIds = allCourses.stream()
                .filter(c -> {
                    if (difficultyRecommendation == 1) return "easy".equals(c.getDifficulty());
                    if (difficultyRecommendation == 2) return "medium".equals(c.getDifficulty());
                    if (difficultyRecommendation == 3) return "hard".equals(c.getDifficulty()) || "medium".equals(c.getDifficulty());
                    return true;
                })
                .map(Course::getId)
                .limit(5)
                .collect(Collectors.toList());

        level.setLevel(difficultyLevel);
        level.setCompletionRate(completionRate);
        level.setTotalMinutes(totalMinutes);
        level.setDifficultyRecommendation(difficultyRecommendation);
        level.setSuitableCourseIds(suitableCourseIds);

        return level;
    }

    private List<RehabilitationTag> identifyRehabilitationNeeds(User user) {
        List<RehabilitationTag> tags = new ArrayList<>();

        User.HealthProfile healthProfile = user.getHealthProfile();
        List<String> recentSearches = user.getRecentSearches();

        List<Course> allCourses = jsonDataService.readJsonFile(COURSES_FILE, new TypeReference<List<Course>>() {});

        if (healthProfile != null) {
            if (healthProfile.isHasArthritis()) {
                RehabilitationTag tag = new RehabilitationTag();
                tag.setTag("关节不适");
                tag.setDescription("存在关节炎情况，需要保护关节的舞蹈运动");
                tag.setSeverity("中度");
                tag.setRecommendedCourseIds(allCourses.stream()
                        .filter(c -> c.getSuitableFor().stream().anyMatch(s -> s.contains("关节炎") || s.contains("膝盖")))
                        .map(Course::getId)
                        .limit(3)
                        .collect(Collectors.toList()));
                tags.add(tag);
            }

            if (healthProfile.isHasHypertension()) {
                RehabilitationTag tag = new RehabilitationTag();
                tag.setTag("血压问题");
                tag.setDescription("高血压用户，适合平缓的舞蹈运动");
                tag.setSeverity("中度");
                tag.setRecommendedCourseIds(allCourses.stream()
                        .filter(c -> c.getSuitableFor().stream().anyMatch(s -> s.contains("高血压") || s.contains("血压")))
                        .map(Course::getId)
                        .limit(3)
                        .collect(Collectors.toList()));
                tags.add(tag);
            }

            if (healthProfile.isHasHeartDisease()) {
                RehabilitationTag tag = new RehabilitationTag();
                tag.setTag("心脏健康");
                tag.setDescription("心脏病患者，适合低强度的舒缓运动");
                tag.setSeverity("重度");
                tag.setRecommendedCourseIds(allCourses.stream()
                        .filter(c -> c.getSuitableFor().stream().anyMatch(s -> s.contains("心脏") || s.contains("血压")))
                        .map(Course::getId)
                        .limit(3)
                        .collect(Collectors.toList()));
                tags.add(tag);
            }
        }

        if (recentSearches != null) {
            Map<String, List<String>> searchIssueMap = new HashMap<>();
            searchIssueMap.put("肩颈", Arrays.asList("肩颈酸痛", "颈椎", "肩膀"));
            searchIssueMap.put("腰背", Arrays.asList("腰疼", "腰背不适", "腰间盘", "腰椎"));
            searchIssueMap.put("睡眠", Arrays.asList("失眠", "睡眠质量", "睡不着"));
            searchIssueMap.put("膝盖", Arrays.asList("膝盖", "腿脚"));

            for (Map.Entry<String, List<String>> entry : searchIssueMap.entrySet()) {
                String issue = entry.getKey();
                List<String> keywords = entry.getValue();

                boolean found = recentSearches.stream()
                        .anyMatch(s -> keywords.stream().anyMatch(k -> s.contains(k)));

                if (found) {
                    List<String> courseIds = allCourses.stream()
                            .filter(c -> c.getSuitableFor().stream()
                                    .anyMatch(s -> s.contains(issue) || keywords.stream().anyMatch(k -> s.contains(k))))
                            .map(Course::getId)
                            .limit(3)
                            .collect(Collectors.toList());

                    if (!courseIds.isEmpty()) {
                        RehabilitationTag tag = new RehabilitationTag();
                        tag.setTag(issue + "不适");
                        tag.setDescription("根据您的搜索记录，您可能关注" + issue + "健康问题");
                        tag.setSeverity("轻度");
                        tag.setRecommendedCourseIds(courseIds);
                        tags.add(tag);
                    }
                }
            }
        }

        return tags;
    }

    private List<CourseRecommendation> generateCourseRecommendations(User user, HealthReport report) {
        List<CourseRecommendation> recommendations = new ArrayList<>();

        List<Course> allCourses = jsonDataService.readJsonFile(COURSES_FILE, new TypeReference<List<Course>>() {});
        Set<String> addedCourseIds = new HashSet<>();

        PhysicalAssessment physical = report.getPhysicalAssessment();
        if (physical != null && "舒缓拉伸类舞蹈".equals(physical.getRecommendedDanceType())) {
            List<Course> stretchCourses = allCourses.stream()
                    .filter(c -> c.getSuitableFor().stream().anyMatch(s ->
                            s.contains("关节炎") || s.contains("膝盖") || s.contains("肩颈") ||
                            s.contains("颈椎") || s.contains("腰椎")))
                    .filter(c -> !addedCourseIds.contains(c.getId()))
                    .limit(2)
                    .collect(Collectors.toList());

            for (Course course : stretchCourses) {
                CourseRecommendation rec = new CourseRecommendation();
                rec.setCourseId(course.getId());
                rec.setCourseTitle(course.getTitle());
                rec.setReason("根据身体机能评估，适合舒缓拉伸类舞蹈");
                rec.setMatchLevel("高");
                rec.setCategory("身体机能");
                recommendations.add(rec);
                addedCourseIds.add(course.getId());
            }
        }

        EmotionalState emotional = report.getEmotionalState();
        if (emotional != null) {
            String danceType = emotional.getRecommendedDanceType();
            List<Course> emotionCourses = allCourses.stream()
                    .filter(c -> !addedCourseIds.contains(c.getId()))
                    .limit(2)
                    .collect(Collectors.toList());

            for (Course course : emotionCourses) {
                CourseRecommendation rec = new CourseRecommendation();
                rec.setCourseId(course.getId());
                rec.setCourseTitle(course.getTitle());
                rec.setReason("根据情绪状态，推荐" + danceType);
                rec.setMatchLevel("高");
                rec.setCategory("情绪调节");
                recommendations.add(rec);
                addedCourseIds.add(course.getId());
            }
        }

        List<String> favoriteCourses = user.getFavorites();
        if (favoriteCourses != null && !favoriteCourses.isEmpty()) {
            Map<String, Long> categoryCount = new HashMap<>();
            for (String courseId : favoriteCourses) {
                allCourses.stream()
                        .filter(c -> c.getId().equals(courseId))
                        .findFirst()
                        .ifPresent(c -> categoryCount.merge(c.getCategory(), 1L, Long::sum));
            }

            if (!categoryCount.isEmpty()) {
                String popularCategory = categoryCount.entrySet().stream()
                        .max(Map.Entry.comparingByValue())
                        .map(Map.Entry::getKey)
                        .orElse(null);

                if (popularCategory != null) {
                    List<Course> prefCourses = allCourses.stream()
                            .filter(c -> c.getCategory().equals(popularCategory))
                            .filter(c -> !addedCourseIds.contains(c.getId()))
                            .limit(2)
                            .collect(Collectors.toList());

                    for (Course course : prefCourses) {
                        CourseRecommendation rec = new CourseRecommendation();
                        rec.setCourseId(course.getId());
                        rec.setCourseTitle(course.getTitle());
                        rec.setReason("基于您偏好的" + popularCategory + "风格推荐");
                        rec.setMatchLevel("非常高");
                        rec.setCategory("兴趣偏好");
                        recommendations.add(rec);
                        addedCourseIds.add(course.getId());
                    }
                }
            }
        }

        List<RehabilitationTag> rehabTags = report.getRehabilitationTags();
        if (rehabTags != null && !rehabTags.isEmpty()) {
            for (RehabilitationTag tag : rehabTags) {
                if (tag.getRecommendedCourseIds() != null) {
                    for (String courseId : tag.getRecommendedCourseIds()) {
                        if (addedCourseIds.contains(courseId)) continue;

                        final String fid = courseId;
                        Course course = allCourses.stream()
                                .filter(c -> c.getId().equals(fid))
                                .findFirst()
                                .orElse(null);

                        if (course != null) {
                            CourseRecommendation rec = new CourseRecommendation();
                            rec.setCourseId(course.getId());
                            rec.setCourseTitle(course.getTitle());
                            rec.setReason("针对" + tag.getTag() + "的专项改善");
                            rec.setMatchLevel("非常高");
                            rec.setCategory("康复需求");
                            recommendations.add(rec);
                            addedCourseIds.add(course.getId());
                        }

                        if (recommendations.size() >= 6) break;
                    }
                }
                if (recommendations.size() >= 6) break;
            }
        }

        return recommendations.stream().limit(6).collect(Collectors.toList());
    }
}