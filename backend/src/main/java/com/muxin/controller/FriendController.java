package com.muxin.controller;

import com.muxin.model.User;
import com.muxin.model.FriendRequest;
import com.muxin.service.UserService;
import com.muxin.service.FriendRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/friends")
public class FriendController {

    @Autowired
    private UserService userService;

    @Autowired
    private FriendRequestService friendRequestService;

    @PostMapping("/add")
    public ResponseEntity<Map<String, Object>> addFriend(@RequestBody Map<String, String> request) {
        Map<String, Object> result = new HashMap<>();
        try {
            String userId = request.get("userId");
            String nickname = request.get("nickname");

            if (userId == null || nickname == null) {
                result.put("code", 400);
                result.put("message", "参数不能为空");
                return ResponseEntity.badRequest().body(result);
            }

            User friend = userService.getUserByNickname(nickname);
            if (friend == null) {
                result.put("code", 404);
                result.put("message", "用户不存在");
                return ResponseEntity.status(404).body(result);
            }

            if (friend.getId().equals(userId)) {
                result.put("code", 400);
                result.put("message", "不能添加自己为好友");
                return ResponseEntity.badRequest().body(result);
            }

            userService.addFriend(userId, friend.getId());

            result.put("code", 200);
            result.put("message", "添加好友成功");
            result.put("data", friend);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", e.getMessage());
            return ResponseEntity.status(500).body(result);
        }
    }

    @DeleteMapping("/remove")
    public ResponseEntity<Map<String, Object>> removeFriend(@RequestBody Map<String, String> request) {
        Map<String, Object> result = new HashMap<>();
        try {
            String userId = request.get("userId");
            String friendId = request.get("friendId");

            if (userId == null || friendId == null) {
                result.put("code", 400);
                result.put("message", "参数不能为空");
                return ResponseEntity.badRequest().body(result);
            }

            userService.removeFriend(userId, friendId);

            result.put("code", 200);
            result.put("message", "删除好友成功");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", e.getMessage());
            return ResponseEntity.status(500).body(result);
        }
    }

    @GetMapping("/list/{userId}")
    public ResponseEntity<Map<String, Object>> getFriends(@PathVariable String userId) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<User> friends = userService.getFriends(userId);
            result.put("code", 200);
            result.put("message", "success");
            result.put("data", friends);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", e.getMessage());
            return ResponseEntity.status(500).body(result);
        }
    }

    @PostMapping("/request")
    public ResponseEntity<Map<String, Object>> sendFriendRequest(@RequestBody Map<String, String> request) {
        Map<String, Object> result = new HashMap<>();
        try {
            String userId = request.get("userId");
            String nickname = request.get("nickname");

            if (userId == null || nickname == null) {
                result.put("code", 400);
                result.put("message", "参数不能为空");
                return ResponseEntity.badRequest().body(result);
            }

            FriendRequest req = friendRequestService.sendRequest(userId, nickname);
            result.put("code", 200);
            result.put("message", "申请已发送，等待对方同意");
            result.put("data", req);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("code", 400);
            result.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    @GetMapping("/request/pending/{userId}")
    public ResponseEntity<Map<String, Object>> getPendingRequests(@PathVariable String userId) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<FriendRequest> list = friendRequestService.getPendingRequests(userId);
            result.put("code", 200);
            result.put("message", "success");
            result.put("data", list);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", e.getMessage());
            return ResponseEntity.status(500).body(result);
        }
    }

    @PostMapping("/request/handle")
    public ResponseEntity<Map<String, Object>> handleFriendRequest(@RequestBody Map<String, String> request) {
        Map<String, Object> result = new HashMap<>();
        try {
            String requestId = request.get("requestId");
            String status = request.get("status");

            if (requestId == null || status == null) {
                result.put("code", 400);
                result.put("message", "参数不能为空");
                return ResponseEntity.badRequest().body(result);
            }

            FriendRequest req = friendRequestService.handleRequest(requestId, status);
            result.put("code", 200);
            result.put("message", "处理成功");
            result.put("data", req);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", e.getMessage());
            return ResponseEntity.status(500).body(result);
        }
    }
}