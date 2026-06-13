package com.muxin.controller;

import com.muxin.model.ApiResponse;
import com.muxin.model.Course;
import com.muxin.service.CourseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/courses")
public class CourseController {

    private static final Logger logger = LoggerFactory.getLogger(CourseController.class);

    @Autowired
    private CourseService courseService;

    @Value("${file.data-path}")
    private String dataPath;

    @PostMapping("/upload-video")
    public ApiResponse<String> uploadVideo(@RequestParam("file") MultipartFile file) {
        logger.info("上传视频文件: {}", file.getOriginalFilename());
        if (file.isEmpty()) {
            return ApiResponse.error(400, "文件为空");
        }

        try {
            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null ? originalFilename.substring(originalFilename.lastIndexOf(".")) : ".mp4";
            String newFileName = UUID.randomUUID().toString() + extension;

            // 确保目录存在
            File mediaDir = new File(dataPath, "media");
            if (!mediaDir.exists()) {
                mediaDir.mkdirs();
            }

            // 保存文件
            File dest = new File(mediaDir, newFileName);
            file.transferTo(dest);

            // 返回可以通过服务器访问的URL
            String videoUrl = "http://localhost:8080/media/" + newFileName;
            return ApiResponse.success("上传成功", videoUrl);

        } catch (IOException e) {
            e.printStackTrace();
            return ApiResponse.error(500, "文件上传失败: " + e.getMessage());
        }
    }

    @GetMapping
    public ApiResponse<List<Course>> getAllCourses() {
        List<Course> courses = courseService.getAllCourses();
        return ApiResponse.success(courses);
    }

    @GetMapping("/{id}")
    public ApiResponse<Course> getCourseById(@PathVariable String id) {
        Course course = courseService.getCourseById(id);
        if (course != null) {
            return ApiResponse.success(course);
        }
        return ApiResponse.error(404, "课程不存在");
    }

    @GetMapping("/category/{category}")
    public ApiResponse<List<Course>> getCoursesByCategory(@PathVariable String category) {
        List<Course> courses = courseService.getCoursesByCategory(category);
        return ApiResponse.success(courses);
    }

    @GetMapping("/search")
    public ApiResponse<List<Course>> searchCourses(@RequestParam String keyword) {
        List<Course> courses = courseService.searchCourses(keyword);
        return ApiResponse.success(courses);
    }

    @GetMapping("/recommend")
    public ApiResponse<List<Course>> getRecommendedCourses() {
        List<Course> courses = courseService.getRecommendedCourses();
        return ApiResponse.success(courses);
    }

    @PostMapping("/{id}/like")
    public ApiResponse<Course> likeCourse(@PathVariable String id) {
        Course course = courseService.likeCourse(id);
        if (course != null) {
            return ApiResponse.success("点赞成功", course);
        }
        return ApiResponse.error(404, "课程不存在");
    }

    @GetMapping("/categories")
    public ApiResponse<List<String>> getAllCategories() {
        List<String> categories = courseService.getAllCategories();
        return ApiResponse.success(categories);
    }

    @GetMapping("/safe")
    public ApiResponse<List<Course>> getSafeArticles() {
        List<Course> courses = courseService.getSafeArticles();
        return ApiResponse.success(courses);
    }
}
