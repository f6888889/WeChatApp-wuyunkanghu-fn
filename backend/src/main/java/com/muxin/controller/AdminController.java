package com.muxin.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.muxin.model.ApiResponse;
import com.muxin.service.JsonDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    @Autowired
    private JsonDataService jsonDataService;

    // Mapping of type to filename
    private static final Map<String, String> FILE_MAP = new HashMap<>();
    static {
        FILE_MAP.put("users", "users.json");
        FILE_MAP.put("shop_items", "shop_items.json");
        FILE_MAP.put("messages", "messages.json");
        FILE_MAP.put("learning_records", "learning_records.json");
        FILE_MAP.put("health_reminders", "health_reminders.json");
        FILE_MAP.put("health_alerts", "health_alerts.json");
        FILE_MAP.put("courses", "courses.json");
    }

    @GetMapping("/{type}")
    public ApiResponse<List<Map<String, Object>>> listData(@PathVariable String type) {
        String fileName = FILE_MAP.get(type);
        if (fileName == null) {
            return ApiResponse.error(400, "不支持的数据类型: " + type);
        }
        logger.info("管理员获取数据列表: type={}", type);
        List<Map<String, Object>> data = jsonDataService.readJsonFile(fileName, new TypeReference<List<Map<String, Object>>>() {});
        return ApiResponse.success(data);
    }

    @PostMapping("/{type}")
    public ApiResponse<Map<String, Object>> createData(@PathVariable String type, @RequestBody Map<String, Object> newItem) {
        String fileName = FILE_MAP.get(type);
        if (fileName == null) {
            return ApiResponse.error(400, "不支持的数据类型: " + type);
        }
        
        logger.info("管理员创建数据: type={}, item={}", type, newItem);
        List<Map<String, Object>> data = jsonDataService.readJsonFile(fileName, new TypeReference<List<Map<String, Object>>>() {});
        
        // Ensure ID exists if not provided
        if (!newItem.containsKey("id") || newItem.get("id") == null || newItem.get("id").toString().isEmpty()) {
            newItem.put("id", UUID.randomUUID().toString().substring(0, 8));
        }
        
        data.add(newItem);
        jsonDataService.writeJsonFile(fileName, data);
        return ApiResponse.success("创建成功", newItem);
    }

    @PutMapping("/{type}/{id}")
    public ApiResponse<Map<String, Object>> updateData(@PathVariable String type, @PathVariable String id, @RequestBody Map<String, Object> updatedItem) {
        String fileName = FILE_MAP.get(type);
        if (fileName == null) {
            return ApiResponse.error(400, "不支持的数据类型: " + type);
        }
        
        logger.info("管理员更新数据: type={}, id={}, item={}", type, id, updatedItem);
        List<Map<String, Object>> data = jsonDataService.readJsonFile(fileName, new TypeReference<List<Map<String, Object>>>() {});
        
        boolean found = false;
        for (int i = 0; i < data.size(); i++) {
            Map<String, Object> item = data.get(i);
            if (id.equals(String.valueOf(item.get("id")))) {
                updatedItem.put("id", id); // Ensure ID is preserved
                data.set(i, updatedItem);
                found = true;
                break;
            }
        }
        
        if (found) {
            jsonDataService.writeJsonFile(fileName, data);
            return ApiResponse.success("更新成功", updatedItem);
        }
        return ApiResponse.error(404, "记录未找到");
    }

    @DeleteMapping("/{type}/{id}")
    public ApiResponse<Void> deleteData(@PathVariable String type, @PathVariable String id) {
        String fileName = FILE_MAP.get(type);
        if (fileName == null) {
            return ApiResponse.error(400, "不支持的数据类型: " + type);
        }
        
        logger.info("管理员删除数据: type={}, id={}", type, id);
        List<Map<String, Object>> data = jsonDataService.readJsonFile(fileName, new TypeReference<List<Map<String, Object>>>() {});
        
        boolean removed = data.removeIf(item -> id.equals(String.valueOf(item.get("id"))));
        
        if (removed) {
            jsonDataService.writeJsonFile(fileName, data);
            return ApiResponse.success("删除成功", null);
        }
        return ApiResponse.error(404, "记录未找到");
    }
}
