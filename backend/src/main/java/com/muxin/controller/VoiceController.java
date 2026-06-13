package com.muxin.controller;

import com.muxin.model.ApiResponse;
import com.muxin.service.VoiceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/voice")
public class VoiceController {

    private static final Logger logger = LoggerFactory.getLogger(VoiceController.class);

    @Autowired
    private VoiceService voiceService;

    @PostMapping("/recognize")
    public ApiResponse<Map<String, String>> recognize(@RequestParam("file") MultipartFile file) {
        try {
            logger.info("收到语音识别请求: fileName={}, size={}", file.getOriginalFilename(), file.getSize());
            
            String text = voiceService.recognize(file.getBytes());
            
            logger.info("语音识别结果: {}", text);
            return ApiResponse.success(Map.of("text", text));
        } catch (Exception e) {
            logger.error("语音识别失败", e);
            return ApiResponse.error(500, "语音识别失败: " + e.getMessage());
        }
    }
}
