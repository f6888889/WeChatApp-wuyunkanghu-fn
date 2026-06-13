package com.muxin.controller;

import com.muxin.model.ApiResponse;
import com.muxin.model.Podcast;
import com.muxin.service.PodcastService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/podcasts")
public class PodcastController {

    private static final Logger logger = LoggerFactory.getLogger(PodcastController.class);

    @Autowired
    private PodcastService podcastService;

    @GetMapping
    public ApiResponse<List<Podcast>> getAllPodcasts() {
        logger.info("获取所有播客数据");
        List<Podcast> podcasts = podcastService.getAllPodcasts();
        return ApiResponse.success(podcasts);
    }

    @GetMapping("/{id}")
    public ApiResponse<Podcast> getPodcastById(@PathVariable String id) {
        logger.info("获取播客详情, id: {}", id);
        Podcast podcast = podcastService.getPodcastById(id);
        if (podcast != null) {
            return ApiResponse.success(podcast);
        }
        return ApiResponse.error(404, "播客不存在");
    }

    @GetMapping("/search")
    public ApiResponse<List<Podcast>> searchPodcasts(@RequestParam String keyword) {
        logger.info("搜索播客, keyword: {}", keyword);
        List<Podcast> results = podcastService.searchPodcasts(keyword);
        return ApiResponse.success(results);
    }
}
