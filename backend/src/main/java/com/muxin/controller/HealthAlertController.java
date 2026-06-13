package com.muxin.controller;

import com.muxin.model.ApiResponse;
import com.muxin.model.HealthAlert;
import com.muxin.model.User;
import com.muxin.service.HealthAlertService;
import com.muxin.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/health-alert")
public class HealthAlertController {

    @org.springframework.beans.factory.annotation.Value("${app.strict-content:false}")
    private boolean strictMode;

    private static final Logger logger = LoggerFactory.getLogger(HealthAlertController.class);

    @Autowired
    private HealthAlertService healthAlertService;

    @Autowired
    private UserService userService;

    @GetMapping("/user/{userId}")
    public ApiResponse<List<HealthAlert>> getUserAlerts(@PathVariable String userId) {
        logger.info("获取用户健康预警: userId={}", userId);
        List<HealthAlert> alerts = healthAlertService.getAlertsByUserId(userId);
        return ApiResponse.success(alerts);
    }

    @GetMapping("/user/{userId}/unread")
    public ApiResponse<List<HealthAlert>> getUnreadAlerts(@PathVariable String userId) {
        logger.info("获取用户未读预警: userId={}", userId);
        List<HealthAlert> alerts = healthAlertService.getUnreadAlerts(userId);
        return ApiResponse.success(alerts);
    }

    @PostMapping("/check/{userId}")
    public ApiResponse<HealthAlert> checkAndCreateAlert(@PathVariable String userId) {
        logger.info("检查并创建健康预警: userId={}", userId);
        HealthAlert alert = healthAlertService.checkAndCreateAlert(userId);
        if (alert != null) {
            return ApiResponse.success("检测到异常，已生成预警", alert);
        }
        return ApiResponse.success("未检测到异常", null);
    }

    @PutMapping("/{alertId}/read")
    public ApiResponse<HealthAlert> markAsRead(@PathVariable String alertId) {
        logger.info("标记预警为已读: alertId={}", alertId);
        HealthAlert alert = healthAlertService.markAsRead(alertId);
        if (alert != null) {
            return ApiResponse.success("已标记为已读", alert);
        }
        return ApiResponse.error(404, "预警不存在");
    }

    @PutMapping("/user/{userId}/read-all")
    public ApiResponse<Boolean> markAllAsRead(@PathVariable String userId) {
        logger.info("标记所有预警为已读: userId={}", userId);
        boolean result = healthAlertService.markAllAsRead(userId);
        return ApiResponse.success("已标记所有预警为已读", result);
    }

    @PutMapping("/{userId}/emergency-contact")
    public ApiResponse<User> updateEmergencyContact(@PathVariable String userId, @RequestBody User.EmergencyContact contact) {
        if (strictMode) {
            logger.info("审核模式拦截：更新紧急联系人请求已屏蔽");
            return ApiResponse.error(403, "功能维护中，暂不支持修改");
        }
        logger.info("更新紧急联系人: userId={}", userId);
        User user = userService.getUserById(userId);
        if (user == null) {
            return ApiResponse.error(404, "用户不存在");
        }
        user.setEmergencyContact(contact);
        User updated = userService.updateUser(userId, user);
        return ApiResponse.success("紧急联系人已更新", updated);
    }

    @GetMapping("/{userId}/emergency-contact")
    public ApiResponse<User.EmergencyContact> getEmergencyContact(@PathVariable String userId) {
        if (strictMode) {
            logger.info("审核模式拦截：获取紧急联系人请求已屏蔽");
            return ApiResponse.success(null);
        }
        logger.info("获取紧急联系人: userId={}", userId);
        User user = userService.getUserById(userId);
        if (user == null) {
            return ApiResponse.error(404, "用户不存在");
        }
        return ApiResponse.success(user.getEmergencyContact());
    }
}