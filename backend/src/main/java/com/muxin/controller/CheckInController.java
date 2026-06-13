package com.muxin.controller;

import com.muxin.model.ApiResponse;
import com.muxin.model.User;
import com.muxin.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/checkin")
public class CheckInController {

    private static final Logger log = LoggerFactory.getLogger(CheckInController.class);

    @Autowired
    private UserService userService;

    @PostMapping("/{userId}")
    public ApiResponse<Map<String, Object>> checkIn(@PathVariable String userId) {
        log.info("用户打卡: userId={}", userId);
        Map<String, Object> result = userService.checkIn(userId);
        boolean success = (boolean) result.get("success");
        if (success) {
            return ApiResponse.success("打卡成功", result);
        } else {
            String message = (String) result.get("message");
            if ("用户不存在".equals(message)) {
                return ApiResponse.error(404, message);
            }
            return ApiResponse.success(message, result);
        }
    }

    @GetMapping("/{userId}/status")
    public ApiResponse<Map<String, Object>> getCheckInStatus(@PathVariable String userId) {
        log.info("获取打卡状态: userId={}", userId);
        User user = userService.getUserById(userId);
        if (user == null) {
            return ApiResponse.error(404, "用户不存在");
        }

        java.time.LocalDate today = java.time.LocalDate.now();
        String todayStr = today.toString();
        boolean checkedInToday = todayStr.equals(user.getLastCheckInDate());

        int todayPoints = 0;
        if (checkedInToday) {
            todayPoints = 1;
        }

        int nextPoints = 1;

        Map<String, Object> status = new java.util.HashMap<>();
        status.put("checkedInToday", checkedInToday);
        status.put("continuousDays", user.getCheckInContinuousDays());
        status.put("points", user.getPoints());
        status.put("lastCheckInDate", user.getLastCheckInDate() != null ? user.getLastCheckInDate() : "");
        status.put("todayPoints", todayPoints);
        status.put("nextPoints", nextPoints);

        return ApiResponse.success(status);
    }
}
