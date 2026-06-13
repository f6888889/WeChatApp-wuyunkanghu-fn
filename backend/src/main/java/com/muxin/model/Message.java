package com.muxin.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Message {
    private String id;
    private String senderId;
    private String senderName;
    private String senderType;
    private String content;
    private String createTime;
    private boolean deleted;
    private String deletedTime;
    private String receiverId;
    private Boolean privateChat;

    // 向后兼容旧字段
    @JsonProperty("isPrivate")
    public Boolean getIsPrivate() {
        return privateChat;
    }

    @JsonProperty("isPrivate")
    public void setIsPrivate(Boolean isPrivate) {
        this.privateChat = isPrivate;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getSenderType() {
        return senderType;
    }

    public void setSenderType(String senderType) {
        this.senderType = senderType;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public String getDeletedTime() {
        return deletedTime;
    }

    public void setDeletedTime(String deletedTime) {
        this.deletedTime = deletedTime;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public Boolean getPrivateChat() {
        return privateChat;
    }

    public void setPrivateChat(Boolean privateChat) {
        this.privateChat = privateChat;
    }
}