package com.muxin.controller;

import com.muxin.model.Video;
import com.muxin.service.VideoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/video")
public class VideoController {

    @Autowired
    private VideoService videoService;

    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> getAllVideos() {
        Map<String, Object> result = new HashMap<>();
        try {
            List<Video> videos = videoService.getAllVideos();
            result.put("code", 200);
            result.put("message", "success");
            result.put("data", videos);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", e.getMessage());
            return ResponseEntity.status(500).body(result);
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Map<String, Object>> getUserVideos(@PathVariable String userId) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<Video> videos = videoService.getVideosByUserId(userId);
            result.put("code", 200);
            result.put("message", "success");
            result.put("data", videos);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", e.getMessage());
            return ResponseEntity.status(500).body(result);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getVideoById(@PathVariable String id) {
        Map<String, Object> result = new HashMap<>();
        try {
            Video video = videoService.getVideoById(id);
            if (video != null) {
                result.put("code", 200);
                result.put("message", "success");
                result.put("data", video);
                return ResponseEntity.ok(result);
            } else {
                result.put("code", 404);
                result.put("message", "Video not found");
                return ResponseEntity.status(404).body(result);
            }
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", e.getMessage());
            return ResponseEntity.status(500).body(result);
        }
    }

    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadVideo(
            @RequestParam("file") MultipartFile file,
            @RequestParam("userId") String userId,
            @RequestParam("title") String title,
            @RequestParam(value = "description", required = false) String description) {
        Map<String, Object> result = new HashMap<>();
        try {
            String filename = file.getOriginalFilename();
            if (filename != null) {
                String extension = filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
                if (!extension.equals("mp4") && !extension.equals("mov") && !extension.equals("avi") && !extension.equals("mkv")) {
                    result.put("code", 400);
                    result.put("message", "仅支持H264格式视频");
                    return ResponseEntity.badRequest().body(result);
                }
            }
            
            Video video = videoService.uploadVideo(userId, title, description, file);
            result.put("code", 200);
            result.put("message", "Upload successful");
            result.put("data", video);
            return ResponseEntity.ok(result);
        } catch (IOException e) {
            result.put("code", 500);
            result.put("message", "Upload failed: " + e.getMessage());
            return ResponseEntity.status(500).body(result);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteVideo(@PathVariable String id) {
        Map<String, Object> result = new HashMap<>();
        try {
            boolean success = videoService.deleteVideo(id);
            if (success) {
                result.put("code", 200);
                result.put("message", "Delete successful");
                return ResponseEntity.ok(result);
            } else {
                result.put("code", 404);
                result.put("message", "Video not found");
                return ResponseEntity.status(404).body(result);
            }
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", e.getMessage());
            return ResponseEntity.status(500).body(result);
        }
    }

    @PostMapping("/{id}/view")
    public ResponseEntity<Map<String, Object>> incrementViewCount(@PathVariable String id) {
        Map<String, Object> result = new HashMap<>();
        try {
            Video video = videoService.incrementViewCount(id);
            if (video != null) {
                result.put("code", 200);
                result.put("message", "success");
                result.put("data", video);
                return ResponseEntity.ok(result);
            } else {
                result.put("code", 404);
                result.put("message", "Video not found");
                return ResponseEntity.status(404).body(result);
            }
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", e.getMessage());
            return ResponseEntity.status(500).body(result);
        }
    }

    @PostMapping("/{id}/like")
    public ResponseEntity<Map<String, Object>> incrementLikeCount(@PathVariable String id) {
        Map<String, Object> result = new HashMap<>();
        try {
            Video video = videoService.incrementLikeCount(id);
            if (video != null) {
                result.put("code", 200);
                result.put("message", "success");
                result.put("data", video);
                return ResponseEntity.ok(result);
            } else {
                result.put("code", 404);
                result.put("message", "Video not found");
                return ResponseEntity.status(404).body(result);
            }
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", e.getMessage());
            return ResponseEntity.status(500).body(result);
        }
    }
}