package com.muxin.service;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class JsonDataService {

    private static final Logger logger = LoggerFactory.getLogger(JsonDataService.class);

    @Value("${file.data-path}")
    private String dataPath;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .setSerializationInclusion(JsonInclude.Include.NON_NULL);

    @PostConstruct
    public void init() {
        File dataDir = new File(dataPath);
        if (!dataDir.exists()) {
            dataDir.mkdirs();
        }
    }

    public synchronized <T> List<T> readJsonFile(String fileName, TypeReference<List<T>> typeReference) {
        try {
            File file = new File(dataPath, fileName);
            if (file.exists()) {
                if (file.length() == 0) {
                    return new ArrayList<>();
                }
                List<T> result = objectMapper.readValue(file, typeReference);
                return result != null ? result : new ArrayList<>();
            }
        } catch (IOException e) {
            logger.error("读取 JSON 文件失败: " + fileName, e);
            throw new RuntimeException("读取数据失败，请稍后重试", e);
        }
        return new ArrayList<>();
    }

    public synchronized <T> void writeJsonFile(String fileName, List<T> data) {
        try {
            File file = new File(dataPath, fileName);
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, data);
        } catch (IOException e) {
            logger.error("写入 JSON 文件失败: " + fileName, e);
            throw new RuntimeException("保存数据失败", e);
        }
    }

    public <T> T readSingleJsonFile(String fileName, TypeReference<T> typeReference) {
        try {
            File file = new File(dataPath, fileName);
            if (file.exists()) {
                if (file.length() == 0) {
                    return null;
                }
                return objectMapper.readValue(file, typeReference);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
