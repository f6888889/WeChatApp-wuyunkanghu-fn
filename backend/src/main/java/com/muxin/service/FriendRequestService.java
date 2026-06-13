package com.muxin.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.muxin.model.FriendRequest;
import com.muxin.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class FriendRequestService {

    private static final String REQUESTS_FILE = "friend_requests.json";
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private JsonDataService jsonDataService;

    @Autowired
    private UserService userService;

    public synchronized List<FriendRequest> getAllRequests() {
        return jsonDataService.readJsonFile(REQUESTS_FILE, new TypeReference<List<FriendRequest>>() {});
    }

    public synchronized void saveRequests(List<FriendRequest> requests) {
        jsonDataService.writeJsonFile(REQUESTS_FILE, requests);
    }

    public FriendRequest sendRequest(String fromUserId, String toNickname) {
        User fromUser = userService.getUserById(fromUserId);
        if (fromUser == null) {
            throw new RuntimeException("当前发送用户不存在");
        }

        User toUser = userService.getUserByNickname(toNickname);
        if (toUser == null) {
            throw new RuntimeException("用户不存在");
        }

        if (fromUser.getId().equals(toUser.getId())) {
            throw new RuntimeException("不能添加自己为好友");
        }

        // 检查是否已经是好友
        List<User> friends = userService.getFriends(fromUserId);
        boolean isAlreadyFriend = friends.stream().anyMatch(f -> toUser.getId().equals(f.getId()));
        if (isAlreadyFriend) {
            throw new RuntimeException("你们已经是好友了");
        }

        List<FriendRequest> requests = getAllRequests();

        // 检查是否已有自己向对方发送的 Pending 申请
        boolean sentPending = requests.stream()
                .anyMatch(r -> fromUserId.equals(r.getFromUserId()) 
                        && toUser.getId().equals(r.getToUserId()) 
                        && "PENDING".equals(r.getStatus()));
        if (sentPending) {
            throw new RuntimeException("已发送过申请，请等待对方同意");
        }

        // 检查是否对方已经向自己发送了 Pending 申请
        boolean receivedPending = requests.stream()
                .anyMatch(r -> toUser.getId().equals(r.getFromUserId()) 
                        && fromUserId.equals(r.getToUserId()) 
                        && "PENDING".equals(r.getStatus()));
        if (receivedPending) {
            throw new RuntimeException("对方已给您发送过好友申请，请在您的消息列表中通过");
        }

        // 创建新申请
        FriendRequest request = new FriendRequest();
        request.setId("req_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16));
        request.setFromUserId(fromUserId);
        request.setFromUsername(fromUser.getNickname());
        request.setToUserId(toUser.getId());
        request.setToUsername(toUser.getNickname());
        request.setStatus("PENDING");
        request.setCreateTime(LocalDateTime.now().format(formatter));

        requests.add(request);
        saveRequests(requests);

        return request;
    }

    public List<FriendRequest> getPendingRequests(String toUserId) {
        return getAllRequests().stream()
                .filter(r -> toUserId.equals(r.getToUserId()) && "PENDING".equals(r.getStatus()))
                .collect(Collectors.toList());
    }

    public synchronized FriendRequest handleRequest(String requestId, String status) {
        List<FriendRequest> requests = getAllRequests();
        FriendRequest target = null;
        for (FriendRequest r : requests) {
            if (requestId.equals(r.getId())) {
                target = r;
                break;
            }
        }

        if (target == null) {
            throw new RuntimeException("该申请记录不存在");
        }

        if (!"PENDING".equals(target.getStatus())) {
            throw new RuntimeException("该申请已被处理");
        }

        target.setStatus(status);

        if ("ACCEPTED".equals(status)) {
            // 双向添加好友
            userService.addFriend(target.getFromUserId(), target.getToUserId());
            userService.addFriend(target.getToUserId(), target.getFromUserId());
        }

        saveRequests(requests);
        return target;
    }
}
