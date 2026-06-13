package com.muxin.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.muxin.model.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class MessageService {

    private static final String MESSAGES_FILE = "messages.json";

    @Autowired
    private JsonDataService jsonDataService;

    public List<Message> getAllMessages() {
        List<Message> allMessages = jsonDataService.readJsonFile(MESSAGES_FILE, new TypeReference<List<Message>>() {});
        return allMessages.stream()
                .filter(m -> !m.isDeleted())
                .filter(m -> m.getPrivateChat() == null || !m.getPrivateChat())
                .filter(m -> !"private".equals(m.getSenderType()))
                .sorted((m1, m2) -> {
                    if (m1.getCreateTime() == null) return -1;
                    if (m2.getCreateTime() == null) return 1;
                    return m1.getCreateTime().compareTo(m2.getCreateTime());
                })
                .collect(Collectors.toList());
    }

    public Message saveMessage(String senderId, String senderName, String senderType, String content) {
        List<Message> messages = jsonDataService.readJsonFile(MESSAGES_FILE, new TypeReference<List<Message>>() {});
        if (messages == null) messages = new java.util.ArrayList<>();
        
        Message message = new Message();
        message.setId(UUID.randomUUID().toString().substring(0, 8));
        message.setSenderId(senderId);
        message.setSenderName(senderName);
        message.setSenderType(senderType);
        message.setContent(content);
        message.setDeleted(false);
        message.setCreateTime(LocalDateTime.now().toString());
        message.setPrivateChat(false);
        
        messages.add(message);
        jsonDataService.writeJsonFile(MESSAGES_FILE, messages);
        return message;
    }

    public boolean recallMessage(String messageId, String userId) {
        List<Message> messages = jsonDataService.readJsonFile(MESSAGES_FILE, new TypeReference<List<Message>>() {});
        for (Message message : messages) {
            if (messageId.equals(message.getId())) {
                if (!message.isDeleted()) {
                    message.setDeleted(true);
                    message.setDeletedTime(LocalDateTime.now().toString());
                    jsonDataService.writeJsonFile(MESSAGES_FILE, messages);
                    return true;
                }
                return false;
            }
        }
        return false;
    }

    public void deleteAll() {
        jsonDataService.writeJsonFile(MESSAGES_FILE, List.of());
    }

    public Message savePrivateMessage(String senderId, String senderName, String receiverId, String content) {
        List<Message> messages = jsonDataService.readJsonFile(MESSAGES_FILE, new TypeReference<List<Message>>() {});
        if (messages == null) messages = new java.util.ArrayList<>();
        Message message = new Message();
        message.setId(UUID.randomUUID().toString().substring(0, 8));
        message.setSenderId(senderId);
        message.setSenderName(senderName);
        message.setSenderType("private");
        message.setContent(content);
        message.setReceiverId(receiverId);
        message.setDeleted(false);
        message.setCreateTime(LocalDateTime.now().toString());
        message.setPrivateChat(true);
        
        messages.add(message);
        jsonDataService.writeJsonFile(MESSAGES_FILE, messages);
        return message;
    }

    public List<Message> getPrivateMessages(String userId1, String userId2) {
        return jsonDataService.readJsonFile(MESSAGES_FILE, new TypeReference<List<Message>>() {}).stream()
                .filter(m -> "private".equals(m.getSenderType()) || (m.getPrivateChat() != null && m.getPrivateChat()))
                .filter(m -> !m.isDeleted())
                .filter(m -> {
                    boolean match1 = userId1.equals(m.getSenderId()) || userId1.equals(m.getReceiverId());
                    boolean match2 = userId2.equals(m.getSenderId()) || userId2.equals(m.getReceiverId());
                    return match1 && match2;
                })
                .sorted(Comparator.comparing(Message::getCreateTime))
                .collect(Collectors.toList());
    }
}