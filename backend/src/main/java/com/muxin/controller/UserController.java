package com.muxin.controller;

import com.muxin.model.ApiResponse;
import com.muxin.model.User;
import com.muxin.model.LearningRecord;
import com.muxin.service.UserService;
import com.muxin.service.LearningRecordService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private LearningRecordService learningRecordService;

    @Value("${file.data-path:./data}")
    private String dataPath;

    @GetMapping("/{id}")
    public ApiResponse<User> getUserById(@PathVariable String id) {
        logger.info("获取用户信息: userId={}", id);
        User user = userService.getUserById(id);
        if (user != null) {
            return ApiResponse.success(user);
        }
        return ApiResponse.error(404, "用户不存在");
    }

    @GetMapping("/{id}/learning-history")
    public ApiResponse<List<LearningRecord>> getLearningHistory(@PathVariable String id) {
        logger.info("获取用户学习记录: userId={}", id);
        List<LearningRecord> history = learningRecordService.getRecordsByUserId(id);
        return ApiResponse.success(history);
    }

    @PostMapping
    public ApiResponse<User> createUser(@RequestBody User user) {
        logger.info("创建用户: {}", user.getOpenId());
        User created = userService.createUser(user);
        return ApiResponse.success("用户创建成功", created);
    }

    @PutMapping("/{id}")
    public ApiResponse<User> updateUser(@PathVariable String id, @RequestBody User user) {
        logger.info("更新用户信息: userId={}", id);
        User updated = userService.updateUser(id, user);
        if (updated != null) {
            return ApiResponse.success("用户更新成功", updated);
        }
        return ApiResponse.error(404, "用户不存在");
    }

    @PostMapping("/{id}/favorites/{courseId}")
    public ApiResponse<User> addFavorite(@PathVariable String id, @PathVariable String courseId) {
        logger.info("添加收藏: userId={}, courseId={}", id, courseId);
        User user = userService.addFavorite(id, courseId);
        if (user != null) {
            return ApiResponse.success("收藏成功", user);
        }
        return ApiResponse.error(404, "用户不存在");
    }

    @DeleteMapping("/{id}/favorites/{courseId}")
    public ApiResponse<User> removeFavorite(@PathVariable String id, @PathVariable String courseId) {
        logger.info("取消收藏: userId={}, courseId={}", id, courseId);
        User user = userService.removeFavorite(id, courseId);
        if (user != null) {
            return ApiResponse.success("取消收藏成功", user);
        }
        return ApiResponse.error(404, "用户不存在");
    }

    @PostMapping("/{id}/searches")
    public ApiResponse<User> addRecentSearch(@PathVariable String id, @RequestBody String keyword) {
        logger.info("添加搜索记录: userId={}, keyword={}", id, keyword);
        User user = userService.addRecentSearch(id, keyword);
        if (user != null) {
            return ApiResponse.success("搜索记录添加成功", user);
        }
        return ApiResponse.error(404, "用户不存在");
    }

    @PostMapping("/{id}/learning-history")
    public ApiResponse<User> addLearningHistory(@PathVariable String id, @RequestBody User.LearningHistory history) {
        logger.info("添加学习记录: userId={}, courseId={}, duration={}分钟", id, history.getCourseId(), history.getWatchDuration());
        User user = userService.addLearningHistory(id, history);
        if (user != null) {
            return ApiResponse.success("学习记录添加成功", user);
        }
        return ApiResponse.error(404, "用户不存在");
    }

    @PostMapping("/{id}/online-time")
    public ApiResponse<Map<String, Object>> recordOnlineTime(@PathVariable String id, @RequestBody Map<String, Integer> data) {
        int minutes = data.getOrDefault("minutes", 0);
        Map<String, Object> result = userService.addOnlineTime(id, minutes);
        if (result != null) {
            return ApiResponse.success("在线时间记录成功", result);
        }
        return ApiResponse.error(404, "用户不存在");
    }
}
