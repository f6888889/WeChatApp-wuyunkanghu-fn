package com.muxin.controller;

import com.muxin.model.ApiResponse;
import com.muxin.model.Message;
import com.muxin.service.MessageService;
import com.muxin.service.JsonDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private static final Logger logger = LoggerFactory.getLogger(MessageController.class);

    @Autowired
    private MessageService messageService;

    @Autowired
    private JsonDataService jsonDataService;

    @GetMapping("/debug")
    public ApiResponse<List<Message>> debugMessages() {
        logger.info("收到调试请求：读取原始消息文件");
        try {
            List<Message> rawMessages = jsonDataService.readJsonFile("messages.json", new com.fasterxml.jackson.core.type.TypeReference<List<Message>>() {});
            return ApiResponse.success("原始数据", rawMessages);
        } catch (Exception e) {
            logger.error("调试读取失败", e);
            return ApiResponse.error(500, e.getMessage());
        }
    }

    @GetMapping
    public ApiResponse<List<Message>> getAllMessages() {
        // logger.info("收到获取所有消息的请求");
        try {
            List<Message> messages = messageService.getAllMessages();
            // logger.info("成功获取 {} 条消息", messages.size());
            return ApiResponse.success(messages);
        } catch (Exception e) {
            logger.error("获取消息失败", e);
            return ApiResponse.error(500, "获取消息失败: " + e.getMessage());
        }
    }

    @PostMapping
    public ApiResponse<Message> sendMessage(@RequestBody Map<String, String> request) {
        String content = request.get("content");
        String userId = request.get("userId");
        String userName = request.get("userName");
        String senderType = request.get("senderType");

        logger.info("收到发送消息请求: userId={}, userName={}, senderType={}, content={}", 
                   userId, userName, senderType, content);

        if (content == null || content.trim().isEmpty()) {
            logger.warn("发送消息失败: 内容为空");
            return ApiResponse.error(400, "消息内容不能为空");
        }

        try {
            Message userMessage = messageService.saveMessage(
                    userId != null ? userId : "user",
                    userName != null ? userName : "用户",
                    senderType != null ? senderType : "user",
                    content
            );
            logger.info("消息保存成功: id={}", userMessage.getId());
            return ApiResponse.success(userMessage);
        } catch (Exception e) {
            logger.error("消息保存失败", e);
            return ApiResponse.error(500, "消息发送失败: " + e.getMessage());
        }
    }

    @PostMapping("/recall")
    public ApiResponse<Void> recallMessage(@RequestBody Map<String, String> request) {
        String messageId = request.get("messageId");
        String userId = request.get("userId");

        logger.info("撤回消息: messageId={}, userId={}", messageId, userId);

        if (messageId == null || userId == null) {
            return ApiResponse.error(400, "参数不完整");
        }

        boolean success = messageService.recallMessage(messageId, userId);
        if (success) {
            return ApiResponse.success(null);
        } else {
            return ApiResponse.error(403, "撤回失败，消息不存在或已被撤回");
        }
    }

    @DeleteMapping
    public ApiResponse<Void> deleteAllMessages() {
        messageService.deleteAll();
        return ApiResponse.success(null);
    }

    @GetMapping("/private")
    public ApiResponse<List<Message>> getPrivateMessages(
            @RequestParam String userId1,
            @RequestParam String userId2) {
        logger.info("获取私聊消息: userId1={}, userId2={}", userId1, userId2);
        List<Message> messages = messageService.getPrivateMessages(userId1, userId2);
        return ApiResponse.success(messages);
    }

    @PostMapping("/private")
    public ApiResponse<Message> sendPrivateMessage(@RequestBody Map<String, String> request) {
        String content = request.get("content");
        String userId = request.get("userId");
        String userName = request.get("userName");
        String receiverId = request.get("receiverId");

        logger.info("发送私聊消息: userId={}, receiverId={}, content={}", userId, receiverId, content);

        if (content == null || content.trim().isEmpty()) {
            return ApiResponse.error(400, "消息内容不能为空");
        }

        Message userMessage = messageService.savePrivateMessage(
                userId != null ? userId : "user",
                userName != null ? userName : "用户",
                receiverId,
                content
        );

        return ApiResponse.success(userMessage);
    }
}