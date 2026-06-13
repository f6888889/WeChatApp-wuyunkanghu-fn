package com.muxin.service;

import com.muxin.model.Course;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AIService {

    @Autowired
    private CourseService courseService;

    @Autowired
    private DoubaoService doubaoService;

    private static final Map<String, List<String>> SYMPTOM_KEYWORDS = new HashMap<>();

    static {
        SYMPTOM_KEYWORDS.put("膝盖", Arrays.asList("膝盖不适", "关节炎", "术后恢复"));
        SYMPTOM_KEYWORDS.put("腰", Arrays.asList("腰椎不适", "腰间盘突出", "久坐老人"));
        SYMPTOM_KEYWORDS.put("肩颈", Arrays.asList("肩颈酸痛", "颈椎不适", "长期低头"));
        SYMPTOM_KEYWORDS.put("血压", Arrays.asList("高血压", "血压高"));
        SYMPTOM_KEYWORDS.put("血糖", Arrays.asList("糖尿病", "血糖高"));
        SYMPTOM_KEYWORDS.put("心脏", Arrays.asList("心脏病", "心脏不适"));
        SYMPTOM_KEYWORDS.put("腿", Arrays.asList("腿脚不便", "腿部不适"));
        SYMPTOM_KEYWORDS.put("手", Arrays.asList("手部僵硬", "手指不灵活"));
    }

    private static final List<String> MOOD_KEYWORDS = Arrays.asList(
            "开心", "高兴", "快乐", "愉快", "兴奋", "放松", "轻松",
            "无聊", "寂寞", "孤独", "累", "疲惫", "困", "烦躁"
    );

    public List<Course> smartSearch(String query) {
        if (query == null || query.trim().isEmpty()) {
            return courseService.getRecommendedCourses();
        }

        String lowerQuery = query.toLowerCase();

        if (containsAny(lowerQuery, MOOD_KEYWORDS)) {
            return courseService.getRecommendedCourses();
        }

        for (Map.Entry<String, List<String>> entry : SYMPTOM_KEYWORDS.entrySet()) {
            if (lowerQuery.contains(entry.getKey())) {
                return courseService.getAllCourses().stream()
                        .filter(c -> c.getSuitableFor() != null && c.getSuitableFor().stream()
                                .anyMatch(s -> entry.getValue().stream()
                                        .anyMatch(v -> v.toLowerCase().contains(s.toLowerCase()))))
                        .collect(Collectors.toList());
            }
        }

        return courseService.searchCourses(query);
    }

    public List<Course> recommendBasedOnProfile(Map<String, Object> userProfile) {
        List<Course> allCourses = courseService.getAllCourses();

        if (userProfile == null || userProfile.isEmpty()) {
            return courseService.getRecommendedCourses();
        }

        Set<String> healthConditions = new HashSet<>();
        if (Boolean.TRUE.equals(userProfile.get("hasHypertension"))) {
            healthConditions.add("高血压");
        }
        if (Boolean.TRUE.equals(userProfile.get("hasDiabetes"))) {
            healthConditions.add("糖尿病");
        }
        if (Boolean.TRUE.equals(userProfile.get("hasArthritis"))) {
            healthConditions.add("关节炎");
        }
        if (Boolean.TRUE.equals(userProfile.get("hasHeartDisease"))) {
            healthConditions.add("心脏病");
        }
        String mobilityLevel = (String) userProfile.get("mobilityLevel");
        if ("limited".equals(mobilityLevel) || "some_limitation".equals(mobilityLevel)) {
            healthConditions.add("需要舒缓运动");
        }

        if (healthConditions.isEmpty()) {
            return courseService.getRecommendedCourses();
        }

        return allCourses.stream()
                .filter(c -> c.getSuitableFor() != null && c.getSuitableFor().stream()
                        .anyMatch(s -> healthConditions.stream()
                                .anyMatch(h -> s.contains(h) || h.contains(s))))
                .collect(Collectors.toList());
    }

    public String getCompanionResponse(String message) {
        if (message == null || message.trim().isEmpty()) {
            return "我在呢，有什么想说的吗？";
        }

        String contextPrompt = "你是一个贴心的银发族健康陪伴助手，名叫小银。用户是一位老年人。" + message;

        return doubaoService.chat(contextPrompt);
    }

    private boolean containsAny(String text, List<String> keywords) {
        return keywords.stream().anyMatch(text::contains);
    }
}