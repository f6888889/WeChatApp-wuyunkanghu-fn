package com.muxin.controller;

import com.muxin.model.ApiResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/config")
public class ConfigController {

    @Value("${app.strict-content:false}")
    private boolean strictContent;

    @GetMapping("/status")
    public ApiResponse<Map<String, Boolean>> getStatus() {
        Map<String, Boolean> status = new HashMap<>();
        status.put("isReviewMode", strictContent);
        return ApiResponse.success(status);
    }
}
