package com.muxin.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.muxin.config.DoubaoConfig;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DoubaoService {

    private static final Logger logger = LoggerFactory.getLogger(DoubaoService.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private DoubaoConfig doubaoConfig;

    private WebClient webClient;

    @PostConstruct
    public void init() {
        if (doubaoConfig.isEnabled()) {
            this.webClient = WebClient.builder()
                    .baseUrl(doubaoConfig.getBaseUrl())
                    .defaultHeader("Authorization", "Bearer " + doubaoConfig.getApiKey())
                    .build();
        }
    }

    public String chat(String message) {
        if (!doubaoConfig.isEnabled()) {
            return "AI服务暂未启用";
        }

        long startTime = System.currentTimeMillis();
        logger.info("========== AI API 调用开始 ==========");
        logger.info("请求URL: {}", doubaoConfig.getBaseUrl());
        logger.info("请求模型: {}", doubaoConfig.getModel());
        logger.info("请求消息: {}", message);

        try {
            Map<String, Object> body = new HashMap<>();
            body.put("model", doubaoConfig.getModel());
            body.put("messages", List.of(Map.of(
                    "role", "user",
                    "content", message
            )));

            Map response = webClient.post()
                    .uri("/chat/completions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            logger.info("API 调用耗时: {}ms", duration);

            if (response != null && response.containsKey("choices")) {
                List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
                if (choices != null && !choices.isEmpty()) {
                    Map<String, Object> messageObj = (Map<String, Object>) choices.get(0).get("message");
                    String content = (String) messageObj.get("content");
                    logger.info("AI 回复内容: {}", content);
                    logger.info("========== AI API 调用完成 ==========");
                    return content;
                }
            }

            logger.warn("AI 返回结果异常");
            logger.info("========== AI API 调用完成 ==========");
            return "抱歉，AI暂时无法回复您的问题";

        } catch (Exception e) {
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            logger.error("API 调用异常，耗时: {}ms，错误信息: ", duration, e);
            logger.info("========== AI API 调用完成 ==========");
            return "调用AI服务时出现错误：" + e.getMessage();
        }
    }

    public Flux<String> chatStream(String message) {
        if (!doubaoConfig.isEnabled()) {
            return Flux.just("AI服务暂未启用");
        }

        logger.info("========== AI 流式 API 调用开始 ==========");
        logger.info("请求URL: {}", doubaoConfig.getBaseUrl());
        logger.info("请求模型: {}", doubaoConfig.getModel());
        logger.info("请求消息: {}", message);
        long startTime = System.currentTimeMillis();

        try {
            Map<String, Object> body = new HashMap<>();
            body.put("model", doubaoConfig.getModel());
            body.put("messages", List.of(Map.of(
                    "role", "user",
                    "content", message
            )));
            body.put("stream", true);

            ParameterizedTypeReference<ServerSentEvent<String>> type = new ParameterizedTypeReference<>() {};
            StringBuilder fullResponse = new StringBuilder();

            return webClient.post()
                    .uri("/chat/completions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToFlux(type)
                    .filter(event -> event.data() != null && !"[DONE]".equals(event.data().trim()))
                    .flatMap(event -> {
                        try {
                            Map<String, Object> map = objectMapper.readValue(event.data(), Map.class);
                            List<Map<String, Object>> choices = (List<Map<String, Object>>) map.get("choices");
                            if (choices != null && !choices.isEmpty()) {
                                Map<String, Object> delta = (Map<String, Object>) choices.get(0).get("delta");
                                if (delta != null && delta.containsKey("content")) {
                                    String content = (String) delta.get("content");
                                    if (content != null && !content.isEmpty()) {
                                        logger.debug("接收到流式数据块: {}", content);
                                        fullResponse.append(content);
                                        return Flux.just(content);
                                    }
                                }
                            }
                        } catch (Exception e) {
                            logger.error("解析流式响应失败: " + event.data(), e);
                        }
                        return Flux.empty();
                    })
                    .doOnError(error -> {
                        long endTime = System.currentTimeMillis();
                        long duration = endTime - startTime;
                        logger.error("流式API调用异常，耗时: {}ms，错误信息: ", duration, error);
                    })
                    .doOnComplete(() -> {
                        long endTime = System.currentTimeMillis();
                        long duration = endTime - startTime;
                        logger.info("AI 完整回复内容: {}", fullResponse.toString());
                        logger.info("========== AI 流式 API 调用完成，耗时: {}ms ==========", duration);
                    });

        } catch (Exception e) {
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            logger.error("API 调用异常，耗时: {}ms，错误信息: ", duration, e);
            return Flux.just("调用AI服务时出现错误：" + e.getMessage());
        }
    }
}
