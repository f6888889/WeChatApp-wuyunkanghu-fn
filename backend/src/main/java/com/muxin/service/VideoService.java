package com.muxin.service;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.PutObjectRequest;
import com.fasterxml.jackson.core.type.TypeReference;
import com.muxin.model.Video;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class VideoService {

    @Autowired
    private OSS ossClient;

    @Autowired
    private JsonDataService jsonDataService;

    @Value("${aliyun.oss.bucket-name}")
    private String bucketName;

    @Value("${aliyun.oss.endpoint}")
    private String endpoint;

    private static final String VIDEOS_FILE = "videos.json";

    public List<Video> getAllVideos() {
        List<Video> videos = jsonDataService.readJsonFile(VIDEOS_FILE, new TypeReference<List<Video>>() {});
        if (videos == null) {
            return new ArrayList<>();
        }
        return videos;
    }

    public Video getVideoById(String id) {
        List<Video> videos = getAllVideos();
        return videos.stream()
                .filter(v -> v.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    public List<Video> getVideosByUserId(String userId) {
        List<Video> videos = getAllVideos();
        return videos.stream()
                .filter(v -> v.getUserId().equals(userId))
                .toList();
    }

    public Video uploadVideo(String userId, String title, String description, MultipartFile file) throws IOException {
        String fileExtension = getFileExtension(file.getOriginalFilename());
        String objectKey = "videos/" + UUID.randomUUID().toString() + fileExtension;

        PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, objectKey, file.getInputStream());
        ossClient.putObject(putObjectRequest);

        String videoUrl = "https://" + bucketName + "." + endpoint + "/" + objectKey;

        Video video = new Video();
        video.setId(UUID.randomUUID().toString());
        video.setUserId(userId);
        video.setTitle(title);
        video.setDescription(description);
        video.setVideoUrl(videoUrl);
        video.setFileSize(file.getSize());
        video.setDuration(0);
        video.setCreatedAt(LocalDateTime.now());
        video.setUpdatedAt(LocalDateTime.now());
        video.setViewCount(0);
        video.setLikeCount(0);

        List<Video> videos = getAllVideos();
        videos.add(video);
        jsonDataService.writeJsonFile(VIDEOS_FILE, videos);

        return video;
    }

    public boolean deleteVideo(String id) {
        List<Video> videos = getAllVideos();
        Video video = videos.stream()
                .filter(v -> v.getId().equals(id))
                .findFirst()
                .orElse(null);
        
        if (video == null) {
            return false;
        }

        String objectKey = getObjectKeyFromUrl(video.getVideoUrl());
        if (objectKey != null) {
            ossClient.deleteObject(bucketName, objectKey);
        }

        videos.remove(video);
        jsonDataService.writeJsonFile(VIDEOS_FILE, videos);
        return true;
    }

    public Video incrementViewCount(String id) {
        List<Video> videos = getAllVideos();
        Video video = videos.stream()
                .filter(v -> v.getId().equals(id))
                .findFirst()
                .orElse(null);
        
        if (video != null) {
            video.setViewCount(video.getViewCount() + 1);
            video.setUpdatedAt(LocalDateTime.now());
            jsonDataService.writeJsonFile(VIDEOS_FILE, videos);
        }
        return video;
    }

    public Video incrementLikeCount(String id) {
        List<Video> videos = getAllVideos();
        Video video = videos.stream()
                .filter(v -> v.getId().equals(id))
                .findFirst()
                .orElse(null);
        
        if (video != null) {
            video.setLikeCount(video.getLikeCount() + 1);
            video.setUpdatedAt(LocalDateTime.now());
            jsonDataService.writeJsonFile(VIDEOS_FILE, videos);
        }
        return video;
    }

    private String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf('.') == -1) {
            return ".mp4";
        }
        return filename.substring(filename.lastIndexOf('.'));
    }

    private String getObjectKeyFromUrl(String url) {
        if (url == null) {
            return null;
        }
        int index = url.indexOf(bucketName + "." + endpoint + "/");
        if (index != -1) {
            return url.substring(index + (bucketName + "." + endpoint + "/").length());
        }
        return null;
    }
}