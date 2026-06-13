package com.muxin.controller;

import com.muxin.model.ApiResponse;
import com.muxin.model.Course;
import com.muxin.service.AIService;
import com.muxin.service.DoubaoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class AIController {

    private static final Logger logger = LoggerFactory.getLogger(AIController.class);

    @Autowired
    private AIService aiService;

    @Autowired
    private DoubaoService doubaoService;

    @PostMapping("/search")
    public ApiResponse<List<Course>> smartSearch(@RequestBody Map<String, String> request) {
        String query = request.get("query");
        logger.info("AI智能搜索: query={}", query);
        List<Course> courses = aiService.smartSearch(query);
        return ApiResponse.success(courses);
    }

    @PostMapping("/recommend")
    public ApiResponse<List<Course>> recommend(@RequestBody Map<String, Object> userProfile) {
        logger.info("AI推荐课程");
        List<Course> courses = aiService.recommendBasedOnProfile(userProfile);
        return ApiResponse.success(courses);
    }

    @GetMapping(value = "/companion/chat")
    public ResponseEntity<SseEmitter> chat(@RequestParam String message) {
        logger.info("AI陪伴聊天（流式）: message={}", message);
        String contextPrompt = "你是一个贴心的银发族健康陪伴助手，名叫小银。用户是一位老年人。" + message;
        
        // 创建一个2分钟超时的 SseEmitter
        SseEmitter emitter = new SseEmitter(120000L);
        
        // 订阅流式数据并异步发送给前端
        doubaoService.chatStream(contextPrompt)
                .map(content -> {
                    // 构建标准 SSE 格式中的 JSON 部分
                    Map<String, Object> delta = new HashMap<>();
                    delta.put("content", content);
                    
                    Map<String, Object> choice = new HashMap<>();
                    choice.put("delta", delta);
                    
                    Map<String, Object> sseData = new HashMap<>();
                    sseData.put("choices", Collections.singletonList(choice));
                    
                    try {
                        return new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(sseData);
                    } catch (Exception e) {
                        logger.error("JSON序列化失败", e);
                        return "";
                    }
                })
                .subscribe(
                    data -> {
                        try {
                            // SseEmitter.event().data(data) 会自动添加 "data:" 前缀 and 换行符，并立即刷新输出流
                            emitter.send(SseEmitter.event().data(data));
                        } catch (Exception e) {
                            logger.error("发送流式块失败", e);
                        }
                    },
                    error -> {
                        logger.error("流式读取异常", error);
                        emitter.completeWithError(error);
                    },
                    () -> {
                        try {
                            // 发送结束标志
                            emitter.send(SseEmitter.event().data("[DONE]"));
                            emitter.complete();
                        } catch (Exception e) {
                            logger.error("发送结束标志失败", e);
                        }
                    }
                );

        return ResponseEntity.ok()
                .header("X-Accel-Buffering", "no")
                .header("Cache-Control", "no-cache")
                .header("Connection", "keep-alive")
                .contentType(MediaType.TEXT_EVENT_STREAM)
                .body(emitter);
    }
}
