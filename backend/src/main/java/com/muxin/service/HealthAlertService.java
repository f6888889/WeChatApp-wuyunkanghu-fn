package com.muxin.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.muxin.model.HealthAlert;
import com.muxin.model.HealthAlert.HealthDataSnapshot;
import com.muxin.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class HealthAlertService {

    private static final Logger logger = LoggerFactory.getLogger(HealthAlertService.class);
    private static final String ALERTS_FILE = "health_alerts.json";

    @Autowired
    private JsonDataService jsonDataService;

    @Autowired
    private UserService userService;

    @org.springframework.beans.factory.annotation.Value("${app.strict-content:false}")
    private boolean strictMode;

    private static final int MAX_DAILY_MINUTES = 300;
    private static final int RECOMMENDED_MINUTES = 180;

    public List<HealthAlert> getAllAlerts() {
        return jsonDataService.readJsonFile(ALERTS_FILE, new TypeReference<List<HealthAlert>>() {});
    }

    public List<HealthAlert> getAlertsByUserId(String userId) {
        return getAllAlerts().stream()
                .filter(a -> userId.equals(a.getUserId()))
                .sorted(Comparator.comparing(HealthAlert::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }

    public List<HealthAlert> getUnreadAlerts(String userId) {
        return getAllAlerts().stream()
                .filter(a -> userId.equals(a.getUserId()) && !a.isRead())
                .collect(Collectors.toList());
    }

    public HealthAlert checkAndCreateAlert(String userId) {
        User user = userService.getUserById(userId);
        if (user == null) return null;

        User.LearningProgress progress = user.getLearningProgress();
        if (progress == null) return null;

        int todayMinutes = progress.getTodayOnlineMinutes();
        int recommended = progress.getDailyRecommendedMinutes();
        if (recommended == 0) recommended = RECOMMENDED_MINUTES;

        HealthAlert alert = null;

        if (todayMinutes >= MAX_DAILY_MINUTES) {
            alert = createMaxDurationAlert(user, todayMinutes);
        } else if (todayMinutes > recommended * 1.5) {
            alert = createStudyOverloadAlert(user, todayMinutes, recommended);
        } else if (todayMinutes == 0) {
            LocalDate today = LocalDate.now();
            String lastDate = progress.getLastOnlineDate();
            if (lastDate != null && !lastDate.equals(today.toString())) {
                alert = createNoActivityAlert(user);
            }
        } else if (todayMinutes > recommended) {
            alert = createStudyOvertimeAlert(user, todayMinutes, recommended);
        }

        if (alert != null) {
            List<HealthAlert> alerts = getAllAlerts();
            alerts.add(alert);
            jsonDataService.writeJsonFile(ALERTS_FILE, alerts);
            sendNotifications(user, alert);
        }

        return alert;
    }

    private void sendNotifications(User user, HealthAlert alert) {
        if (strictMode) {
            logger.info("审核模式：已静默拦截预警通知发送 (userId={})", user.getId());
            return;
        }
        logger.info("发送预警通知: userId={}, alertType={}", user.getId(), alert.getAlertType());

        logger.info("=== 通知模拟 ===");
        logger.info("【发送给用户】用户: {}, 内容: {}", user.getNickname(), alert.getContent());

        User.EmergencyContact contact = user.getEmergencyContact();
        if (contact != null && contact.isEnabled() && contact.getPhone() != null) {
            String contactMessage = String.format("【银舞沐心紧急通知】尊敬的家属您好，%s（手机号：%s）今日学习时长已达%d分钟，超过了健康上限5小时，请及时关注并提醒其休息。",
                    user.getNickname() != null ? user.getNickname() : "用户",
                    user.getPhone() != null ? user.getPhone() : "未知",
                    alert.getDataSnapshot() != null && alert.getDataSnapshot().size() > 0
                        ? parseMinutesFromSnapshot(alert.getDataSnapshot().get(0).getCurrentValue())
                        : 0);

            logger.info("【发送给紧急联系人】联系人: {}, 电话: {}, 关系: {}",
                    contact.getName(), contact.getPhone(), contact.getRelationship());
            logger.info("【消息内容】: {}", contactMessage);
            logger.info("====================");
        } else {
            logger.info("【紧急联系人未设置或未启用】");
        }
    }

    private int parseMinutesFromSnapshot(String value) {
        if (value == null) return 0;
        try {
            String num = value.replaceAll("[^0-9]", "");
            return num.isEmpty() ? 0 : Integer.parseInt(num);
        } catch (Exception e) {
            return 0;
        }
    }

    private HealthAlert createMaxDurationAlert(User user, int todayMinutes) {
        HealthAlert alert = new HealthAlert();
        alert.setId(UUID.randomUUID().toString());
        alert.setUserId(user.getId());
        alert.setUserName(user.getNickname());
        alert.setAlertType("MAX_DURATION_EXCEEDED");
        alert.setAlertLevel("HIGH");
        alert.setTitle("已达每日学习上限");
        alert.setContent(String.format("您好，%s今日学习时长已达%d分钟，超过了每日5小时的上限！为保护您的健康，请立即停止学习，适当休息。",
                user.getNickname() != null ? user.getNickname() : "用户", todayMinutes));
        alert.setNotified(false);
        alert.setRead(false);
        alert.setCreatedAt(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        List<HealthDataSnapshot> snapshots = new ArrayList<>();
        HealthDataSnapshot snapshot1 = new HealthDataSnapshot();
        snapshot1.setMetricName("今日学习时长");
        snapshot1.setCurrentValue(todayMinutes + "分钟");
        snapshot1.setExpectedRange("0-" + MAX_DAILY_MINUTES + "分钟");
        snapshot1.setDeviation("已达上限");
        snapshots.add(snapshot1);
        alert.setDataSnapshot(snapshots);

        return alert;
    }

    private HealthAlert createStudyOverloadAlert(User user, int todayMinutes, int recommended) {
        HealthAlert alert = new HealthAlert();
        alert.setId(UUID.randomUUID().toString());
        alert.setUserId(user.getId());
        alert.setUserName(user.getNickname());
        alert.setAlertType("STUDY_OVERLOAD");
        alert.setAlertLevel("MEDIUM");
        alert.setTitle("学习时长严重超标");
        alert.setContent(String.format("您好，%s今日学习时长已达%d分钟，超过了建议时长%d分钟的1.5倍，建议适当休息！",
                user.getNickname() != null ? user.getNickname() : "用户", todayMinutes, recommended));
        alert.setNotified(false);
        alert.setRead(false);
        alert.setCreatedAt(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        List<HealthDataSnapshot> snapshots = new ArrayList<>();
        HealthDataSnapshot snapshot1 = new HealthDataSnapshot();
        snapshot1.setMetricName("今日学习时长");
        snapshot1.setCurrentValue(todayMinutes + "分钟");
        snapshot1.setExpectedRange("0-" + recommended + "分钟");
        snapshot1.setDeviation("+" + (todayMinutes - recommended) + "分钟");
        snapshots.add(snapshot1);
        alert.setDataSnapshot(snapshots);

        return alert;
    }

    private HealthAlert createStudyOvertimeAlert(User user, int todayMinutes, int recommended) {
        HealthAlert alert = new HealthAlert();
        alert.setId(UUID.randomUUID().toString());
        alert.setUserId(user.getId());
        alert.setUserName(user.getNickname());
        alert.setAlertType("STUDY_OVERTIME");
        alert.setAlertLevel("LOW");
        alert.setTitle("学习时长超标");
        alert.setContent(String.format("您好，%s今日学习时长已达%d分钟，超过了建议时长%d分钟，请注意休息。",
                user.getNickname() != null ? user.getNickname() : "用户", todayMinutes, recommended));
        alert.setNotified(false);
        alert.setRead(false);
        alert.setCreatedAt(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        List<HealthDataSnapshot> snapshots = new ArrayList<>();
        HealthDataSnapshot snapshot1 = new HealthDataSnapshot();
        snapshot1.setMetricName("今日学习时长");
        snapshot1.setCurrentValue(todayMinutes + "分钟");
        snapshot1.setExpectedRange("0-" + recommended + "分钟");
        snapshot1.setDeviation("+" + (todayMinutes - recommended) + "分钟");
        snapshots.add(snapshot1);
        alert.setDataSnapshot(snapshots);

        return alert;
    }

    private HealthAlert createNoActivityAlert(User user) {
        HealthAlert alert = new HealthAlert();
        alert.setId(UUID.randomUUID().toString());
        alert.setUserId(user.getId());
        alert.setUserName(user.getNickname());
        alert.setAlertType("NO_ACTIVITY");
        alert.setAlertLevel("INFO");
        alert.setTitle("今日未学习");
        alert.setContent(String.format("您好，%s今日还未开始学习，建议进行适度的舞蹈学习活动。",
                user.getNickname() != null ? user.getNickname() : "用户"));
        alert.setNotified(false);
        alert.setRead(false);
        alert.setCreatedAt(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        return alert;
    }

    public HealthAlert markAsRead(String alertId) {
        List<HealthAlert> alerts = getAllAlerts();
        for (HealthAlert alert : alerts) {
            if (alertId.equals(alert.getId())) {
                alert.setRead(true);
                jsonDataService.writeJsonFile(ALERTS_FILE, alerts);
                return alert;
            }
        }
        return null;
    }

    public boolean markAllAsRead(String userId) {
        List<HealthAlert> alerts = getAllAlerts();
        boolean changed = false;
        for (HealthAlert alert : alerts) {
            if (userId.equals(alert.getUserId()) && !alert.isRead()) {
                alert.setRead(true);
                changed = true;
            }
        }
        if (changed) {
            jsonDataService.writeJsonFile(ALERTS_FILE, alerts);
            return true;
        }
        return false;
    }
}