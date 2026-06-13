package com.muxin.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.muxin.model.Course;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CourseService {

    private static final String COURSES_FILE = "courses.json";

    @Autowired
    private JsonDataService jsonDataService;

    public List<Course> getAllCourses() {
        return jsonDataService.readJsonFile(COURSES_FILE, new TypeReference<List<Course>>() {});
    }

    public Course getCourseById(String id) {
        return getAllCourses().stream()
                .filter(c -> id.equals(c.getId()))
                .findFirst()
                .orElse(null);
    }

    public List<Course> searchCourses(String keyword) {
        List<Course> courses = getAllCourses();
        if (keyword == null || keyword.trim().isEmpty()) {
            return courses;
        }
        String lowerKeyword = keyword.toLowerCase();
        return courses.stream()
                .filter(c -> (c.getTitle() != null && c.getTitle().toLowerCase().contains(lowerKeyword)) ||
                        (c.getDescription() != null && c.getDescription().toLowerCase().contains(lowerKeyword)) ||
                        (c.getSuitableFor() != null && c.getSuitableFor().stream().anyMatch(s -> s.toLowerCase().contains(lowerKeyword))))
                .collect(Collectors.toList());
    }

    public List<Course> getRecommendedCourses() {
        List<Course> courses = getAllCourses();
        return courses.stream().limit(4).collect(Collectors.toList());
    }

    public List<String> getAllCategories() {
        List<Course> courses = getAllCourses();
        return courses.stream()
                .map(Course::getCategory)
                .filter(c -> c != null)
                .distinct()
                .collect(Collectors.toList());
    }

    public List<Course> getCoursesByCategory(String category) {
        List<Course> courses = getAllCourses();
        return courses.stream()
                .filter(c -> category.equals(c.getCategory()))
                .collect(Collectors.toList());
    }

    public Course likeCourse(String id) {
        List<Course> courses = getAllCourses();
        Optional<Course> courseOpt = courses.stream()
                .filter(c -> id.equals(c.getId()))
                .findFirst();
        
        if (courseOpt.isPresent()) {
            Course course = courseOpt.get();
            course.setLikeCount(course.getLikeCount() + 1);
            jsonDataService.writeJsonFile(COURSES_FILE, courses);
            return course;
        }
        return null;
    }

    public List<Course> getSafeArticles() {
        return jsonDataService.readJsonFile("safe_articles.json", new TypeReference<List<Course>>() {});
    }
}