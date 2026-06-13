package com.muxin.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.muxin.model.Podcast;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.stream.Collectors;

@Service
public class PodcastService {

    private static final String PODCASTS_FILE = "podcasts.json";

    @Autowired
    private JsonDataService jsonDataService;

    @Autowired
    private DoubaoService doubaoService;

    public List<Podcast> getAllPodcasts() {
        return jsonDataService.readJsonFile(PODCASTS_FILE, new TypeReference<List<Podcast>>() {});
    }

    public Podcast getPodcastById(String id) {
        return getAllPodcasts().stream()
                .filter(p -> id.equals(p.getId()))
                .findFirst()
                .orElse(null);
    }

    public List<Podcast> searchPodcasts(String keyword) {
        List<Podcast> podcasts = getAllPodcasts();
        if (keyword == null || keyword.trim().isEmpty()) {
            return podcasts;
        }
        String lowerKeyword = keyword.toLowerCase();
        return podcasts.stream()
                .filter(p -> (p.getTitle() != null && p.getTitle().toLowerCase().contains(lowerKeyword)) ||
                        (p.getDescription() != null && p.getDescription().toLowerCase().contains(lowerKeyword)) ||
                        (p.getAuthor() != null && p.getAuthor().toLowerCase().contains(lowerKeyword)))
                .collect(Collectors.toList());
    }

    public Map<String, Object> searchAndChat(String keyword) {
        Map<String, Object> resultMap = new HashMap<>();

        // 1. 调用通义千问大模型获取健康贴士回答
        String aiResponse = "";
        try {
            String prompt = "你是一个贴心、温柔的老年人健康和舞蹈康养助手“小银”。请用非常亲切关怀的语气，用大白话简短地回答用户提出的这个问题，给出专业、易懂的指导建议，字数控制在150字以内：" + keyword;
            aiResponse = doubaoService.chat(prompt);
        } catch (Exception e) {
            aiResponse = "抱歉，AI 助手暂时无法回答，您可以先收听我们推荐的健康播客。";
        }
        resultMap.put("aiResponse", aiResponse);

        // 2. 匹配本地播客
        List<Podcast> allPodcasts = getAllPodcasts();
        List<Podcast> matchedPodcasts = new ArrayList<>();
        if (keyword != null && !keyword.trim().isEmpty()) {
            String lowerKeyword = keyword.toLowerCase();
            matchedPodcasts = allPodcasts.stream()
                    .filter(p -> (p.getTitle() != null && p.getTitle().toLowerCase().contains(lowerKeyword)) ||
                            (p.getDescription() != null && p.getDescription().toLowerCase().contains(lowerKeyword)) ||
                            (p.getAuthor() != null && p.getAuthor().toLowerCase().contains(lowerKeyword)))
                    .collect(Collectors.toList());
        }

        // 3. 如果没有合适的播客，就显示默认的（现有的两条）
        if (matchedPodcasts.isEmpty()) {
            matchedPodcasts = allPodcasts.stream().limit(2).collect(Collectors.toList());
        }

        resultMap.put("podcasts", matchedPodcasts);
        return resultMap;
    }
}
