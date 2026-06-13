package com.muxin.controller;

import com.muxin.model.ApiResponse;
import com.muxin.model.HealthReport;
import com.muxin.model.HealthReminder;
import com.muxin.model.User;
import com.muxin.service.HealthService;
import com.muxin.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/health")
public class HealthController {

    @org.springframework.beans.factory.annotation.Value("${app.strict-content:false}")
    private boolean strictMode;

    private static final Logger logger = LoggerFactory.getLogger(HealthController.class);

    @Autowired
    private HealthService healthService;

    @Autowired
    private UserService userService;

    @GetMapping("/reminders")
    public ApiResponse<List<HealthReminder>> getAllReminders() {
        if (strictMode) {
            logger.info("审核模式拦截：获取所有健康提醒请求已屏蔽");
            return ApiResponse.success(java.util.Collections.emptyList());
        }
        logger.info("获取所有健康提醒");
        List<HealthReminder> reminders = healthService.getAllReminders();
        return ApiResponse.success(reminders);
    }

    @GetMapping("/reminders/daily")
    public ApiResponse<List<HealthReminder>> getDailyReminders() {
        logger.info("获取每日健康提醒");
        List<HealthReminder> reminders = healthService.getDailyReminders();
        return ApiResponse.success(reminders);
    }

    @GetMapping("/tips")
    public ApiResponse<List<HealthReminder>> getHealthTips() {
        logger.info("获取健康小贴士");
        List<HealthReminder> tips = healthService.getHealthTips();
        return ApiResponse.success(tips);
    }

    @GetMapping("/reminders/time/{timeSlot}")
    public ApiResponse<List<HealthReminder>> getRemindersByTimeSlot(@PathVariable String timeSlot) {
        logger.info("获取时段健康提醒: timeSlot={}", timeSlot);
        List<HealthReminder> reminders = healthService.getRemindersByTimeSlot(timeSlot);
        return ApiResponse.success(reminders);
    }

    @GetMapping("/report/{userId}")
    public ApiResponse<HealthReport> getHealthReport(@PathVariable String userId) {
        if (strictMode) {
            logger.info("审核模式拦截：生成健康报告请求已屏蔽");
            return ApiResponse.error(403, "功能维护中，暂不支持查看报告");
        }
        logger.info("生成健康报告: userId={}", userId);
        User user = userService.getUserById(userId);
        if (user == null) {
            return ApiResponse.error(404, "用户不存在");
        }
        HealthReport report = healthService.generateHealthReport(user);
        return ApiResponse.success(report);
    }
}
