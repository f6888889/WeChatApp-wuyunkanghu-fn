package com.muxin.controller;

import com.muxin.model.ApiResponse;
import com.muxin.model.User;
import com.muxin.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${wechat.mock:false}")
    private boolean mockMode;

    @Value("${wechat.appid:}")
    private String appid;

    @Value("${wechat.secret:}")
    private String secret;

    @PostMapping("/login")
    public ApiResponse<Map<String, Object>> wxLogin(@RequestBody Map<String, String> loginData) {
        String code = loginData.get("code");
        log.info("微信登录请求: code={}", code);
        if (code == null || code.isEmpty()) {
            return ApiResponse.error(400, "code不能为空");
        }

        String openid;
        if (mockMode) {
            openid = "mock_" + code;
        } else {
            openid = getOpenIdFromWechat(code);
            if (openid == null) {
                return ApiResponse.error(401, "微信登录失败");
            }
        }

        User user = userService.getUserByOpenId(openid);
        if (user == null) {
            user = new User();
            user.setId(userService.getNextUserId());
            user.setOpenId(openid);
            user.setNickname("新用户");
            user.setFavorites(java.util.Collections.emptyList());
            user.setRecentSearches(java.util.Collections.emptyList());
            User.LearningProgress progress = new User.LearningProgress();
            progress.setContinuousDays(1);
            user.setLearningProgress(progress);
            userService.createUser(user);
        } else {
            User.LearningProgress progress = user.getLearningProgress();
            if (progress == null) {
                progress = new User.LearningProgress();
            }
            String today = java.time.LocalDate.now().toString();
            String lastLoginDate = user.getCreatedAt();
            if (lastLoginDate == null || !lastLoginDate.equals(today)) {
                progress.setContinuousDays(progress.getContinuousDays() + 1);
                user.setCreatedAt(today);
            }
            userService.checkIn(user.getId());
            userService.updateUser(user.getId(), user);
        }

        String token = UUID.randomUUID().toString().replace("-", "");

        Map<String, Object> result = Map.of(
            "token", token,
            "user", user
        );

        return ApiResponse.success("登录成功", result);
    }

    private String getOpenIdFromWechat(String code) {
        try {
            String url = String.format(
                "https://api.weixin.qq.com/sns/jscode2session?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code",
                appid, secret, code
            );
            log.info("请求微信接口: {}", url);
            String response = restTemplate.getForObject(url, String.class);
            log.info("微信返回: {}", response);

            if (response != null && response.contains("openid")) {
                int start = response.indexOf("\"openid\":\"") + 10;
                int end = response.indexOf("\"", start);
                return response.substring(start, end);
            } else if (response != null && response.contains("errcode")) {
                log.error("微信接口返回错误: {}", response);
            }
        } catch (Exception e) {
            log.error("微信接口调用失败", e);
        }
        return null;
    }

    @GetMapping("/user/{id}")
    public ApiResponse<User> getUserInfo(@PathVariable String id) {
        User user = userService.getUserById(id);
        if (user != null) {
            return ApiResponse.success(user);
        }
        return ApiResponse.error(404, "用户不存在");
    }
}
