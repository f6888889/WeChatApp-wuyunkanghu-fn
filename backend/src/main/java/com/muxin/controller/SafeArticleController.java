package com.muxin.controller;

import com.muxin.model.ApiResponse;
import com.muxin.model.Course;
import com.muxin.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/safe_articles")
public class SafeArticleController {

    @Autowired
    private CourseService courseService;

    @GetMapping
    public ApiResponse<List<Course>> getSafeArticles() {
        List<Course> articles = courseService.getSafeArticles();
        return ApiResponse.success(articles);
    }
}
