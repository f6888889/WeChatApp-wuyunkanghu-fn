package com.muxin.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.muxin.model.User;
import com.muxin.model.LearningRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserService {

    private static final String USERS_FILE = "users.json";
    private static final int DAILY_POINTS_CAP = 5;

    @Autowired
    private JsonDataService jsonDataService;

    @Autowired
    private LearningRecordService learningRecordService;

    @org.springframework.beans.factory.annotation.Value("${app.strict-content:false}")
    private boolean strictMode;

    private User scrubUser(User user) {
        if (user != null && strictMode) {
            user.setEmergencyContact(null);
        }
        return user;
    }

    public List<User> getAllUsers() {
        return jsonDataService.readJsonFile(USERS_FILE, new TypeReference<List<User>>() {});
    }

    public User getUserById(String id) {
        User user = getAllUsers().stream()
                .filter(u -> id.equals(u.getId()))
                .findFirst()
                .orElse(null);
        return scrubUser(user);
    }

    public User getUserByOpenId(String openId) {
        User user = getAllUsers().stream()
                .filter(u -> openId.equals(u.getOpenId()))
                .findFirst()
                .orElse(null);
        return scrubUser(user);
    }

    public String getNextUserId() {
        List<User> users = getAllUsers();
        int maxId = 0;
        for (User user : users) {
            String id = user.getId();
            if (id != null && id.startsWith("user_")) {
                try {
                    int num = Integer.parseInt(id.substring(5));
                    if (num > maxId) {
                        maxId = num;
                    }
                } catch (NumberFormatException ignored) {}
            }
        }
        return String.format("user_%04d", maxId + 1);
    }

    public User createUser(User user) {
        List<User> users = getAllUsers();
        if (user.getCreatedAt() == null) {
            user.setCreatedAt(LocalDate.now().toString());
        }
        if (user.getLearningProgress() == null) {
            user.setLearningProgress(new User.LearningProgress());
        }
        if (user.getLearningProgress().getDailyRecommendedMinutes() == 0) {
            user.getLearningProgress().setDailyRecommendedMinutes(180);
        }
        users.add(user);
        jsonDataService.writeJsonFile(USERS_FILE, users);
        return user;
    }

    public User updateUser(String id, User updatedUser) {
        List<User> users = getAllUsers();
        for (int i = 0; i < users.size(); i++) {
            if (id.equals(users.get(i).getId())) {
                updatedUser.setId(id);
                users.set(i, updatedUser);
                jsonDataService.writeJsonFile(USERS_FILE, users);
                return updatedUser;
            }
        }
        return null;
    }

    public User addFavorite(String userId, String courseId) {
        List<User> users = getAllUsers();
        for (User user : users) {
            if (userId.equals(user.getId())) {
                if (user.getFavorites() == null) {
                    user.setFavorites(new ArrayList<>());
                }
                if (!user.getFavorites().contains(courseId)) {
                    user.getFavorites().add(courseId);
                    jsonDataService.writeJsonFile(USERS_FILE, users);
                }
                return user;
            }
        }
        return null;
    }

    public User removeFavorite(String userId, String courseId) {
        List<User> users = getAllUsers();
        for (User user : users) {
            if (userId.equals(user.getId())) {
                if (user.getFavorites() != null) {
                    user.getFavorites().remove(courseId);
                    jsonDataService.writeJsonFile(USERS_FILE, users);
                }
                return user;
            }
        }
        return null;
    }

    public User addRecentSearch(String userId, String keyword) {
        List<User> users = getAllUsers();
        for (User user : users) {
            if (userId.equals(user.getId())) {
                if (user.getRecentSearches() == null) {
                    user.setRecentSearches(new ArrayList<>());
                }
                user.getRecentSearches().remove(keyword);
                user.getRecentSearches().add(0, keyword);
                if (user.getRecentSearches().size() > 10) {
                    user.setRecentSearches(user.getRecentSearches().subList(0, 10));
                }
                jsonDataService.writeJsonFile(USERS_FILE, users);
                return user;
            }
        }
        return null;
    }

    public User addLearningHistory(String userId, User.LearningHistory history) {
        List<User> users = getAllUsers();
        for (User user : users) {
            if (userId.equals(user.getId())) {
                // 保存记录到新的存储
                LearningRecord record = new LearningRecord();
                record.setUserId(userId);
                record.setCourseId(history.getCourseId());
                record.setCourseTitle(history.getCourseTitle());
                record.setCoverImage(history.getCoverImage());
                record.setWatchDuration(history.getWatchDuration());
                record.setTotalDuration(history.getTotalDuration());
                record.setWatchedAt(history.getWatchedAt());
                learningRecordService.addRecord(record);

                if (user.getLearningProgress() == null) {
                    user.setLearningProgress(new User.LearningProgress());
                }
                User.LearningProgress progress = user.getLearningProgress();
                progress.setTotalMinutes(progress.getTotalMinutes() + history.getWatchDuration());

                // 计算连续学习天数
                LocalDate today = LocalDate.now();
                String todayStr = today.toString();
                
                // 获取上一条记录（不包括刚才加的那条）来判断连续性
                List<LearningRecord> historyList = learningRecordService.getRecordsByUserId(userId);
                String lastDateStr = null;
                // 注意：刚才 addRecord 已经把新记录加进去了，所以上一条是 index 1
                if (historyList.size() > 1 && historyList.get(1).getWatchedAt() != null) {
                    lastDateStr = historyList.get(1).getWatchedAt();
                    if (lastDateStr.length() > 10) {
                        lastDateStr = lastDateStr.substring(0, 10);
                    }
                }

                if (lastDateStr == null) {
                    progress.setContinuousDays(1);
                } else if (!lastDateStr.equals(todayStr)) {
                    String yesterdayStr = today.minusDays(1).toString();
                    if (lastDateStr.equals(yesterdayStr)) {
                        progress.setContinuousDays(progress.getContinuousDays() + 1);
                    } else {
                        progress.setContinuousDays(1);
                    }
                }
                
                jsonDataService.writeJsonFile(USERS_FILE, users);
                return user;
            }
        }
        return null;
    }

    public Map<String, Object> addOnlineTime(String userId, int minutes) {
        List<User> users = getAllUsers();
        for (User user : users) {
            if (userId.equals(user.getId())) {
                if (user.getLearningProgress() == null) {
                    user.setLearningProgress(new User.LearningProgress());
                }
                User.LearningProgress progress = user.getLearningProgress();

                LocalDate today = LocalDate.now();
                String todayStr = today.toString();
                String lastDate = progress.getLastOnlineDate();

                if (lastDate == null || !lastDate.equals(todayStr)) {
                    progress.setTodayOnlineMinutes(minutes);
                    progress.setLastOnlineDate(todayStr);
                    progress.setTodayEarnedPoints(0);
                    progress.setTodayVideoPoints(0);
                } else {
                    progress.setTodayOnlineMinutes(progress.getTodayOnlineMinutes() + minutes);
                }

                // 计算视频积分
                // 10min -> 1, 30min -> 2, 60min -> 3, 90min -> 4, 120min -> 5
                int todayMinutes = progress.getTodayOnlineMinutes();
                int targetVideoPoints = 0;
                if (todayMinutes >= 120) {
                    targetVideoPoints = 5;
                } else if (todayMinutes >= 90) {
                    targetVideoPoints = 4;
                } else if (todayMinutes >= 60) {
                    targetVideoPoints = 3;
                } else if (todayMinutes >= 30) {
                    targetVideoPoints = 2;
                } else if (todayMinutes >= 10) {
                    targetVideoPoints = 1;
                }

                int pointsEarned = 0;
                int videoPointsToAdd = targetVideoPoints - progress.getTodayVideoPoints();
                if (videoPointsToAdd > 0) {
                    int currentTodayPoints = progress.getTodayEarnedPoints();
                    int pointsCanAdd = Math.min(videoPointsToAdd, DAILY_POINTS_CAP - currentTodayPoints);
                    if (pointsCanAdd > 0) {
                        user.setPoints(user.getPoints() + pointsCanAdd);
                        progress.setTodayEarnedPoints(currentTodayPoints + pointsCanAdd);
                        progress.setTodayVideoPoints(progress.getTodayVideoPoints() + pointsCanAdd);
                        pointsEarned = pointsCanAdd;
                    }
                }

                progress.setOnlineMinutes(progress.getOnlineMinutes() + minutes);
                jsonDataService.writeJsonFile(USERS_FILE, users);
                
                Map<String, Object> result = new HashMap<>();
                result.put("user", user);
                result.put("pointsEarned", pointsEarned);
                return result;
            }
        }
        return null;
    }

    public User getUserByNickname(String nickname) {
        return getAllUsers().stream()
                .filter(u -> nickname.equals(u.getNickname()))
                .findFirst()
                .orElse(null);
    }

    public User addFriend(String userId, String friendId) {
        List<User> users = getAllUsers();
        for (User user : users) {
            if (userId.equals(user.getId())) {
                if (user.getFriends() == null) {
                    user.setFriends(new ArrayList<>());
                }
                if (!user.getFriends().contains(friendId)) {
                    user.getFriends().add(friendId);
                    jsonDataService.writeJsonFile(USERS_FILE, users);
                }
                return user;
            }
        }
        return null;
    }

    public User removeFriend(String userId, String friendId) {
        List<User> users = getAllUsers();
        for (User user : users) {
            if (userId.equals(user.getId())) {
                if (user.getFriends() != null) {
                    user.getFriends().remove(friendId);
                    jsonDataService.writeJsonFile(USERS_FILE, users);
                }
                return user;
            }
        }
        return null;
    }

    public List<User> getFriends(String userId) {
        User user = getUserById(userId);
        if (user != null && user.getFriends() != null) {
            List<String> friendIds = user.getFriends();
            List<User> friends = new ArrayList<>();
            for (String friendId : friendIds) {
                User friend = getUserById(friendId);
                if (friend != null) {
                    friends.add(friend);
                }
            }
            return friends;
        }
        return new ArrayList<>();
    }

    public Map<String, Object> checkIn(String userId) {
        List<User> users = getAllUsers();
        Map<String, Object> result = new HashMap<>();

        for (User user : users) {
            if (userId.equals(user.getId())) {
                LocalDate today = LocalDate.now();
                String todayStr = today.toString();
                String lastCheckInDate = user.getLastCheckInDate();
                int currentContinuousDays = user.getCheckInContinuousDays();
                int currentPoints = user.getPoints();

                if (lastCheckInDate != null && lastCheckInDate.equals(todayStr)) {
                    result.put("success", false);
                    result.put("message", "今日已打卡");
                    result.put("continuousDays", currentContinuousDays);
                    result.put("points", currentPoints);
                    return result;
                }

                int newContinuousDays;
                if (lastCheckInDate == null) {
                    newContinuousDays = 1;
                } else {
                    LocalDate lastDate = LocalDate.parse(lastCheckInDate);
                    if (lastDate.equals(today.minusDays(1))) {
                        newContinuousDays = currentContinuousDays + 1;
                    } else {
                        newContinuousDays = 1;
                    }
                }

                if (user.getLearningProgress() == null) {
                    user.setLearningProgress(new User.LearningProgress());
                }
                User.LearningProgress progress = user.getLearningProgress();
                
                // 重置今日积分（如果日期变了）
                String lastOnlineDate = progress.getLastOnlineDate();
                if (lastOnlineDate == null || !lastOnlineDate.equals(todayStr)) {
                    progress.setTodayEarnedPoints(0);
                    progress.setTodayVideoPoints(0);
                    progress.setTodayOnlineMinutes(0);
                    progress.setLastOnlineDate(todayStr);
                }

                int dailyPoints = 1; // 每天打卡 1 积分
                int currentTodayPoints = progress.getTodayEarnedPoints();
                int actualEarned = 0;

                if (currentTodayPoints < DAILY_POINTS_CAP) {
                    actualEarned = Math.min(dailyPoints, DAILY_POINTS_CAP - currentTodayPoints);
                    user.setPoints(user.getPoints() + actualEarned);
                    progress.setTodayEarnedPoints(currentTodayPoints + actualEarned);
                }

                user.setCheckInContinuousDays(newContinuousDays);
                user.setLastCheckInDate(todayStr);
                jsonDataService.writeJsonFile(USERS_FILE, users);

                result.put("success", true);
                result.put("message", actualEarned > 0 ? "打卡成功" : "打卡成功（今日积分已达上限）");
                result.put("continuousDays", newContinuousDays);
                result.put("points", user.getPoints());
                result.put("earnedPoints", actualEarned);
                return result;
            }
        }

        result.put("success", false);
        result.put("message", "用户不存在");
        return result;
    }
}